import Vue            from 'vue';
import store          from '../store';
import VueI18n        from 'vue-i18n';
import { Validator }  from 'vee-validate';
import get            from  'lodash/get';
import {
  mapState
}               from 'vuex';

Vue.use(VueI18n);

VueI18n.prototype._initVM = function(data) {
  const i18n = this;
  data.loading = null;
  this._vm = new Vue({
    data,
    store,
    watch: {
      'configuration.preferences.lang': function() {
        const lang = this.configuration ? this.configuration.preferences.lang : null,
              me   = this;
        if(!lang) {
          return;
        }
        if(process.env.LANGS.indexOf(lang) === -1) {
          throw Error("unknown language '" + lang + "'.");
        }
        if(!(this.messages[lang] && Validator.dictionary[lang])) {
          const p = Promise.all([
            import(/* webpackChunkName:"i18n-" */ './' + lang + '.json'),
            import(/* webpackChunkName:"vee-validate-i18n-" */ 'vee-validate/dist/locale/' + lang + '.js')
          ])
            .then(([custom, locale]) => {
              // if I'm the last update
              if(p === me.loading) {
                // i18n.setLocaleMessage(lang, custom);
                i18n._vm.$set(i18n._vm.messages, lang, custom);
                i18n.locale = lang;
                Validator.addLocale(locale.default || locale);
                Validator.setLocale(lang);
                // = me.$set(me, 'locale', lang);
                me.$set(me, 'loading', null);
              }
              return me.loading || p;
            })
            .catch(err => {
              console.error(err);
            });
          this.$set(this, 'loading', p);
        } else {
          i18n.locale = lang;
          Validator.setLocale(lang);
          this.$set(me, 'loading', null);
        }
      }
    },
    computed: mapState({
      configuration: state => state.configuration.configuration
    })
  });
};

export default new VueI18n({
  locale:         get(store, 'state.configuration.preferences.lang',  ((navigator.languages && navigator.languages[0]) || navigator.language || navigator.userLanguage).toLowerCase().split(/[_-]+/)[0]),
  fallbackLocale: 'en',
  messages:       {}
});
