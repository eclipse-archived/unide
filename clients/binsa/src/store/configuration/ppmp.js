import Vue        from 'vue';

export default (store, prefix = []) => {
  if(!(prefix instanceof Array)) {
    prefix = [prefix];
  }

  store.registerModule(prefix.concat('ppmp'), {
    namespaced: true,
    state:      {
      registry:          {},
      translationCounts: {}
    },
    mutations: {
      setTranslationCountsForConfigId(state, { id, counts }) {
        if(state.translationCounts[id]) {
          Vue.set(state.translationCounts, id, {});
        }
        Vue.set(state.translationCounts, id, counts);
      },
      register(state, { key, configId, deviceId, daemon }) {
        if(daemon) {
          if(!state.registry[key]) {
            Vue.set(state.registry, key, {});
          }
          if(!state.registry[key][configId]) {
            Vue.set(state.registry[key], configId, {});
          }
          Vue.set(state.registry[key][configId], deviceId, () => daemon);
        } else {
          if(state.registry[key] && state.registry[key][configId] && state.registry[key][configId][deviceId]) {
            Vue.delete(state.registry[key][configId], deviceId);
            if(!Object.keys(state.registry[key][configId] || {}).length) {
              Vue.delete(state.registry[key], configId);
            }
          }
        }
      }
    },
    getters: {
      getRegistry: (state) =>
        (key, configId, deviceId) => {
          let s = state.registry[key];
          s = s ? s[configId] : null;
          return s && s[deviceId] ? s[deviceId]() : null;
        },
      getTranslationsFor: (state, getters, rootState) =>
        (configId, deviceId) => {
          return (state.translationCounts[configId] || {})[deviceId] || {};
        },
      getTotalTranslationsFor: (state, getters) =>
        (configId, deviceId) =>
          Object.values(getters.getTranslationsFor(configId, deviceId)).reduce((l, v) => l + v, 0)
    }
  });

  // sync local translation count cache with every commit
  store.subscribe(mutation => {
    if(mutation.type === 'configuration/saveConfiguration') {
      let configs = mutation.payload;
      configs = (configs instanceof Array) ? configs : [configs];
      configs.filter(config => config.type === 'ppmp' || config.type === 'ppm')
        .forEach(config => {
          var counts = Object.entries(config.translate || {}).reduce((o, [deviceId, t]) => {
            o[deviceId] = Object.keys(t || {}).reduce((l, v) => {
              l[v] = Object.keys(t[v]).length;
              return l;
            }, {});
            return o;
          }, {});
          store.commit(prefix.concat('ppmp', 'setTranslationCountsForConfigId').join('/'), {
            id: config._id,
            counts
          });
        });
    }
  });
};
