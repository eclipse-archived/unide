import Vue           from 'vue';
import Vuex          from 'vuex';
import configuration from './configuration';

Vue.use(Vuex);

export default new Vuex.Store({
  strict:    (process.env.NODE_ENV !== 'production'),
  plugins:   [configuration],
  state:     {},
  mutations: {},
  modules:   {
  }
});
