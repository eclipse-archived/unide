module.exports = {
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
      config.module.rules.push({
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
    publicPath: '/files/',
    babel:      {
      presets: [['vue-app', {
        targets: { ie: 9 }
      }]],
      plugins: ['transform-runtime']
    }
  },

  plugins: [{
    src: '~/plugins/ga.js'
  }, {
    src: '~/plugins/prismjs.js'
  }],

  router: {
    base: '/unide/'
    //, fallback: true
  },

  modules: [['~modules/postsIdxPlugin', {}]]
};
