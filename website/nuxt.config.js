export default {
  head: {
    title:         'Welcome',
    titleTemplate: 'Eclipse unide - %s',
    meta:          [{
      charset: 'utf-8'
    }, {
      name:    'viewport',
      content: 'width=device-width, initial-scale=1'
    }, {
      hid:     'description',
      name:    'description',
      content: 'Eclipse Unide: Understand Industry devices'
    }],
    link: [
      { rel: 'icon', type: 'image/x-icon', href: '/unide/favicon.ico' }
    ]
  },
  css: [{
    src:  '~assets/styles.scss',
    lang: 'scss'
  }],
  render: {
    resourceHints: false
  },
  loading: { color: '#50237f' },
  build:   {
    extend(config, ctx) {
      if(ctx.isClient && ctx.isDev) {
        // only fails in client.js in dev mode, so no real need for:
        /* if(!(config.entry.app instanceof Array)) {
          config.entry.app = [ config.entry.app ];
        }
        config.entry.app.unshift('core-js/fn/array/filter'); */
        config.module.rules.push({
          enforce: 'pre',
          test:    /\.(js|vue)$/,
          loader:  'eslint-loader',
          exclude: /(node_modules)/
        });
      }
      config.module.rules.unshift({
        type:    'javascript/auto',
        test:    /schema\.json$/,
        exclude: /node_modules/,
        loader:  'json-schema-loader',
        options: {}
      });
    },
    postcss: {
      plugins: {
        'postcss-custom-properties': false
      }
    },
    extractCSS: true,
    publicPath: '/files/'
  },

  plugins: [{
    src: '~/plugins/prismjs.js'
  }],

  generate: {
    routes: ['machine', 'measurement', 'process'].map((name) => `/specification/${name}-message`)
  },
  router: {
    base: '/unide/',
    scrollBehavior({ hash:selector }, from, savedPosition) {
      if(savedPosition) {
        return savedPosition;
      } else {
        return selector ? { selector } : {};
      }
    },
    extendRoutes(routes, resolve) {
      // alias for default PPMP version
      ['machine', 'measurement', 'process'].forEach((name) => routes.push({
        path:     `/specification/${name}-message`,
        redirect: `/specification/v2/${name}-message`
      }));
    }
    //, fallback: true
  },

  modules: [['~modules/postsIdxPlugin', {}]]
};
