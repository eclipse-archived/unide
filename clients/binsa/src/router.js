import Vue              from 'vue';
import Router           from 'vue-router';
import Home             from 'pages/home';
/*
import PPMP             from 'pages/ppmp';
import Configuration    from 'pages/configuration';
import About            from 'pages/about';
*/

Vue.use(Router);

const routes = [{
  path:      '/',
  component: Home
}, {
  path:      '/index.html',
  component: Home
}, {
  path:      '/ppmp/:configId?/:deviceId?',
  component: () => import(/* webpackChunkName:"ppmp" */ 'pages/ppmp'),
  children:  [{
    path:      '/ppmp/:configId/:deviceId/measurements',
    component: () => import(/* webpackChunkName:"ppmp" */ 'pages/ppmp/measurements'),
    props:     true
  }, {
    path:      '/ppmp/:configId/:deviceId/processes',
    component: () => import(/* webpackChunkName:"ppmp" */ 'pages/ppmp/processes'),
    props:     true
  }, {
    path:      '/ppmp/:configId/:deviceId/messages',
    component: () => import(/* webpackChunkName:"ppmp" */ 'pages/ppmp/messages'),
    props:     true
  }, {
    path:      '*',
    component: function() {
      return import(/* webpackChunkName:"notFound" */'./pages/notFound.vue');
    }
  }]
}, {
  path:      '/about',
  component: () => import(/* webpackChunkName:"about" */ 'pages/about')
}, {
  path:      '/configuration',
  component: () => import(/* webpackChunkName:"configuration" */ 'pages/configuration')
}];

if(process.env.NODE_ENV === 'development') {
  routes.push({
    path:      '/ciHelp',
    component: () => import(/* webpackChunkName:"ciHelper" */ 'pages/ciHelp')
  });
}

routes.push({
  path:      '*',
  component: function() {
    return import(/* webpackChunkName:"notFound" */'./pages/notFound.vue');
  }
});

export default new Router({
  // mode: 'hash',
  mode: 'history',
  base: process.env.BASEPATH || '/',
  routes
});
