import Vue           from 'vue';
import PouchDB       from 'pouchdb-core';
import { uuid }      from 'pouchdb-utils';
import isEqual       from 'lodash/isEqual';
import idbAdapter    from 'pouchdb-adapter-idb';
import websqlAdapter from 'pouchdb-adapter-websql';
import cryptoPouch   from 'crypto-pouch';
import ppmp          from './ppmp';

PouchDB
  .plugin(idbAdapter)
  .plugin(websqlAdapter)
  .plugin(cryptoPouch);

/*
if(process.env.NODE_ENV === 'development') {
  PouchDB.debug.enable('*');
} else {
  PouchDB.debug.disable();
}
*/
// PouchDB.plugin(require('pouchdb-replication')); //enable syncing
// PouchDB.plugin(require('pouchdb-adapter-http')); //enable syncing over http

// default 'anonymous' db:

function createDb(prefix = '') {
  return new PouchDB(prefix + 'configurations', {
// don't force idb to allow websql fallback for iOS
//    adapter: 'idb'
  });
}

function createCryptoDb({ user, password, force }) {
  const newDb = createDb(`${user}-`);
  return (new Promise(function(resolve, reject) {
    newDb.crypto(password, {
      cb: (err, key) => {
        if(err) {
          reject(err);
        }
        resolve(key);
      }
    });
  }))
  // test, if provided password can be used
  .then((/* key */) =>
    newDb.get('preferences')
    .then(() => newDb)
    // catch first to reuse the following 'then'
    .catch(err => {
      // it shall not be an error, if preferences wasn't found. Only if decryption fails
      // TODO: also check for err.constructor.name !== 'PouchError' but uglify-save
      if(err.name !== 'not_found') {
        // is an deletion intented?
        if(force) {
          // destroy and try again
          return newDb.destroy()
            .then(() => createCryptoDb({ user, password }));
        }
        // otherwise it's an actual problem
        return newDb.close()
          .then(() => Promise.reject(err));
      }
      return newDb;
    })
  );
}

export default store => {
  let db = null;
  const namespace = 'configuration';

  store.registerModule(namespace, {
    namespaced: true,
    state:      {
      // null indicates that it hasn't been loaded yet. {...} === is loaded
      credentials:    null,
      configuration:  null,
      expandedCardId: null
    },
    mutations: {
      setExpandedCardId(state, id) {
        state.expandedCardId = id;
      },
      saveConfiguration(state, config) {
        if(!config) {
          throw Error('no config with _id defined');
        }
        if(!(config instanceof Array)) {
          config = [config];
        }
        if(!state.configuration) {
          Vue.set(state, 'configuration', {});
        }
        config.forEach(c => Vue.set(state.configuration, c._id, c));
      },
      deleteConfiguration(state, id) {
        Vue.delete(state.configuration, id);
      },
      resetConfiguration(state) {
        Vue.set(state, 'configuration', null);
        Vue.set(state, 'credentials', null);
      },
      saveCredentials(state, credentials) {
        Vue.set(state, 'credentials', credentials ? {
          user: credentials.user || null
          // , password: credentials.user || null,
        } : null);
      }
    },
    actions: {
      async saveConfiguration({ commit, state }, config) {
        if(config._id && isEqual(config, state.configuration[config._id])) {
          return state.configuration[config._id];
        }
        const res = await db.put(Object.assign({}, config, {
          _id: (!config._id || !config._rev) ? uuid() : config._id
        }));
        commit('saveConfiguration', Object.assign({}, config, {
          _id:  res.id,
          _rev: res.rev
        }));
        return state.configuration[res.id];
      },
      async deleteConfiguration({ commit, state }, id) {
        const conf = db.remove(id, state.configuration[id]._rev);
        commit('deleteConfiguration', id);
        return conf;
      },
      async loadConfigurations(nsStore, credentials) {
        // reset any configuration object to null
        const anonymous = !(credentials && credentials.user && credentials.password);
        if(nsStore.state.configuration) {
          nsStore.commit('resetConfiguration');
        }
        try {
          // close any open db synchronously and set to null
          if(db) {
            try {
              await db.close();
            } catch(e) {
              console.error("Coulnd't close db", e);
            }
            db = null;
          }
          // now start opening with the new given credentials
          if(!anonymous) {
            db = await createCryptoDb(credentials);
            nsStore.commit('saveCredentials', credentials);
          } else {
            // fallback anonymous
            db = createDb();
            nsStore.commit('saveCredentials', credentials);
          }
          // load actual configuration content
          return nsStore.dispatch('afterConfigurations', credentials);
        } catch(err) {
          console.error("couldn't load db:", err);
          // fallback anonymous
          if(credentials) {
            return nsStore.dispatch('loadConfigurations')
            .then(configurations => {
              if(!credentials.force) {
                throw err;
              }
              return configurations;
            });
          }
          // otherwise, there's a real problem
          throw err;
        }
      },
      async afterConfigurations(nsStore, credentials) {
        if(!db) {
          throw new Error('initialize db first');
        }
        // db object should be refreshed, now populate the configuration object
        const checkSingletons = () => {
          const singletons = {
            preferences: {
              lang: ((navigator.languages && navigator.languages[0]) || navigator.language || navigator.userLanguage).toLowerCase().split(/[_-]+/)[0]
            }
          };
          // first login should not have credential popup
          if(!(credentials && credentials.user && credentials.password)) {
            singletons.preferences.fallbackLoginIsAnonymous = true;
          }

          return Promise.all(Object.keys(singletons)
          // potentially no configuration exists (ever) yet
          .filter(s => !(nsStore.state.configuration && nsStore.state.configuration[s]))
          .map(function(_id) {
            const obj = Object.assign({
              _id,
              type: 'singleton'
            }, singletons[_id]);
            // TODO: optimize for batch
            return db.put(obj)
            .then(res => nsStore.commit('saveConfiguration', Object.assign({
              _id:  res.id,
              _rev: res.rev
            }, obj)));
          })
        )
        .then(() => {
          // should fallback configuration (credentials = null) be regarded as explicit anonymous (credentials = {}) ?
          if(nsStore.state.configuration.preferences.fallbackLoginIsAnonymous && !nsStore.state.credentials) {
            nsStore.commit('saveCredentials', credentials || {});
          }
          //  logout sets fallbackLoginIsAnonymous explicitly to false, see signOut()
          if(credentials && credentials.remember && !nsStore.state.configuration.preferences.fallbackLoginIsAnonymous) {
            return nsStore.dispatch('saveConfiguration', Object.assign({}, nsStore.state.configuration.preferences, {
              fallbackLoginIsAnonymous: true
            }))
            .then(() => nsStore.state.configuration);
          }
          return nsStore.state.configuration;
        });
        },
        res  = await db.allDocs({
          /* eslint camelcase: 0,indent: 0 */
          include_docs: true
        }),
        docs     = res.rows.map(doc => doc.doc),
          listener = db.changes({
          since:        'now',
          live:         true,
          include_docs: true
        })
        .on('change', function(change) {
          if(change.deleted) {
            nsStore.commit('deleteConfiguration', change.id);
          } else if(!isEqual(nsStore.state.configuration[change.doc._id], change.doc)) {
            nsStore.commit('saveConfiguration', change.doc);
          }
          return checkSingletons();
        });
        if(docs.length) {
          nsStore.commit('saveConfiguration', docs);
        }
        db.on('closed', function() {
          listener.cancel();
        });
        return checkSingletons();
      },
      async signOut(nsStore) {
        const prefs = nsStore.state.configuration.preferences;
        if(prefs && prefs.fallbackLoginIsAnonymous) {
          await nsStore.dispatch('saveConfiguration', Object.assign({}, prefs, {
            fallbackLoginIsAnonymous: false
          }));
        }
        await nsStore.dispatch('loadConfigurations', null);
      }
    }
  });

  // register further modules
  ppmp(store, namespace);

  // load default db
  store.dispatch(`${namespace}/loadConfigurations`);
};
