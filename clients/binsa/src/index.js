import Vue from 'vue';
import App from './app';
import 'es6-promise/auto'; // needed for webpack dynamic module loading
import Sw  from 'offline-plugin/runtime'; // mapped to null-loader in development

if(process.env.NODE_ENV !== 'development') {
  const idle = function() {};
  Sw.install({
    // the new sw should take control immediately
    onUpdateReady: () => {
      Sw.applyUpdate();
    },
    onUpdating:     idle,
    // reload the page to make use of new sw version
    //  window.location.reload();
    onUpdated:      idle,
    onUpdateFailed: idle,
    onInstalled:    idle,
    onError:        idle
  });
}

new Vue(App).$mount('#main');

// if started in nw.js, allow/fake 'multiple instances'
if(window.nw) {
  nw.App.on('open', function(cmdline) {
    const win = nw.Window.get();
    nw.Window.open(location.href, {
      /* eslint camelcase: 0 */
      new_instance: true,
      width:        win.width,
      height:       win.height
    }, newWin => {

    });
  });
}
