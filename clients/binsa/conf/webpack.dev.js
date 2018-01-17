const webpack                         = require('webpack'),
      path                            = require('path'),
      fs                              = require('fs'),
      ExtractTextPlugin               = require('extract-text-webpack-plugin'),
      LodashModuleReplacementPlugin   = require('lodash-webpack-plugin'),
      CopyWebpackPlugin               = require('copy-webpack-plugin'),
      OfflinePlugin                   = require('offline-plugin'),
      HtmlWebpackPlugin               = require('html-webpack-plugin'),
      // FaviconsWebpackPlugin          = require('favicons-webpack-plugin'),
      LicensePlugin                   = require('./webpack-license-plugin'),
      // HtmlCriticalPlugin              = require('html-critical-webpack-plugin'),
      packageJson                     = require('../package.json'),

      extractCSS                      = new ExtractTextPlugin({
        filename: '[name].css'
      }),
      sassLoader = {
        loader:  'sass-loader',
        options: {
          sourceMap:    true,
          includePaths: ['node_modules']
        }
      },
      langs      = fs.readdirSync(`${__dirname}/../src/i18n`).filter(f => f.endsWith('.json')).map(file => file.replace(/.json$/, '')),
      publicPath  = process.env.PREFIX || '/';

module.exports = {
  devtool: 'source-map',
  entry:   {
    app:     './src',
    sandbox: './src/sandbox'
  },

  output: {
    path:       path.join(__dirname, '../dist', publicPath),
    filename:   '[name].js',
    pathinfo:   true,
    publicPath: publicPath
  },

  target: "web",
  /*
  function(compiler) { //shall work in both, node-webkit and chrome app (web)
 		compiler.apply(
      new (require("webpack/lib/JsonpTemplatePlugin"))(compiler.options.output),
 			new (require('webpack/lib/FunctionModulePlugin'))(compiler.options.output),
      new (require("webpack/lib/node/NodeSourcePlugin"))(compiler.options.node),
 			new (require("webpack/lib/ExternalsPlugin"))('commonjs', ['nw.gui', 'os']),
      new (require("webpack/lib/LoaderTargetPlugin"))('web')
 		);
 	},*/

  node: {
    // node polyfills
  },

  devServer: {
    hot:                false,
    inline:             true,
    progress:           true,
    https:              false,
    // historyApiFallback for SPA, needs special handling for sw.js to redirect without host
    historyApiFallback: {
      rewrites: [{
        from: /sw.js$/,
        to:   `${publicPath}sw.js`
      }, {
        from: /.*/,
        to:   `${publicPath}index.html`
      }]
    },
    proxy: {
      '/rest/v2/measurement': {
        target: 'http://unide.eclipse.org:443'
        //, agent: require('http-proxy-agent')('http://localhost:3128')
      }
    }
  },

  module: {
    rules: [{
      test:    /\.vue$/i,
      loader:  'vue-loader',
      options: {
        loaders: {
          js:  '', // needed here to not fallback to babel-loader
          css: extractCSS.extract({
            fallback: 'vue-style-loader',
            use:      [{
              loader:  'css-loader',
              options: {
                sourceMap: true,
                minimize:  false
              }
            }]
          }),
          scss: extractCSS.extract({
            fallback: 'vue-style-loader',
            use:      [{
              loader:  'css-loader',
              options: {
                sourceMap: true,
                minimize:  false
              }
            }, sassLoader]
          }),
          sass: extractCSS.extract({
            fallback: 'vue-style-loader',
            use:      [{
              loader:  'css-loader',
              options: {
                sourceMap: true,
                minimize:  false
              }
            }, sassLoader]
          })
        }
      }
    }, {
      // it might be possible to ignore some dependencies that are not used
      test:    /\.js$/i,
      include: [/chacha/, /ripemd160/],
      use:     ['null-loader']
    }, {
      test:    /\.js$/i,
      exclude: /node_modules/,
      use:     ['eslint-loader']
    }, {
      test: /\.css$/i,
      use:  extractCSS.extract({
        fallback: 'style-loader',
        use:      {
          loader:  'css-loader',
          options: {
            sourceMap: true,
            minimize:  false
          }
        }
      })
    }, {
      test: /\.(scss|sass)$/i,
      use:  extractCSS.extract({
        fallback: 'css-loader',
        use:      sassLoader
      })
    }, {
      test:    /\.(png|jpe?g|gif)(\?.*)?$/i,
      exclude: /img\/icons/,
      use:     [{
        loader:  'url-loader',
        options: {
          limit: 1000,
          name:  '[name].[ext]'
        }
      }]
    }, {
      test: /\.*?$/,
      use:  [{
        loader:  'file-loader',
        options: {
          name: '[name].[ext]'
        }
      }],
      include: /img\/icons/
    }, {
      test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/i,
      use:  [{
        loader:  'url-loader',
        options: {
          limit:    10000,
          mimetype: 'application/font-woff',
          name:     '[name].[ext]'
        }
      }]
    }, {
      test: /\.svg?$/i,
      use:  [{
        loader:  'file-loader',
        options: {
          name: '[name].[ext]'
        }
      }]
    }]
  },

  plugins: fs.readdirSync(`${__dirname}/../src/manifests`).map(f => new HtmlWebpackPlugin({
    filename:    f,
    template:    `${__dirname}/../src/manifests/${f}`,
    inject:      false,
    packageJson: packageJson
    // , minimize:   true
  })).concat([
    new LicensePlugin(),
    // new DashboardPlugin(),
    new HtmlWebpackPlugin({
      filename:      packageJson.main,
      template:      `${__dirname}/../index.html`,
      excludeChunks: ['sandbox'],
      packageJson:   packageJson
    }),
    new HtmlWebpackPlugin({
      filename:      'sandbox.html',
      template:      `${__dirname}/../sandbox.html`,
      excludeChunks: ['app']
    }),
    /*
    new FaviconsWebpackPlugin({
      inject:   true,
      logo:   './src/img/unide.png',
      background: '#fff',
      title:    packageJson.name,
      icons:    {
        appleStartup: false,
        windows:    true
      },
      prefix: 'icons/'
    }),
    */
    new CopyWebpackPlugin([{
      from: 'src/img/icons',
      to:   'icons'
    }, {
      from: 'package.json'
    }, {
      from: 'src/background.js'
    }]),
    extractCSS,
    new webpack.EnvironmentPlugin({
      NODE_ENV: 'development',
      BASEPATH: publicPath,
      LANGS:    langs,
      BRAND:    packageJson.name
    }),
    // limit the locales of vee-validate to the ones we know 
    new webpack.ContextReplacementPlugin(/vee-validate[\/\\]dist[\/\\]locale$/, new RegExp(langs.join('|'))),
    new LodashModuleReplacementPlugin({
      collection: true,
      path:       true
    }),
    new webpack.LoaderOptionsPlugin({
      minimize: false,
      debug:    true
    }),
    new webpack.optimize.CommonsChunkPlugin({
      minChunks:    2, // module => module.context && (module.context.indexOf('node_modules') !== -1),
      children:     true,
      deepChildren: true
    }),
    // display relative path of module on Hot Module Replacement
    new webpack.NamedModulesPlugin(),
    /* new HtmlCriticalPlugin({
      base:     `${__dirname}/../dist`,
      src:      `${publicPath}index.html`,
      dest:     `${__dirname}/../dist${publicPath}index.html`,
      inline:   true,
      minify:   true,
      extract:  true,
      dimensions: [{
        // Galaxy S5
        height: 640,
        width:  360
      }, {
        // iPad Pro
        height: 1366,
        width:  1024
      }, {
        // laptop
        height: 768,
        width:  1366
      }, {
        height: 768,
        width:  1400
      }],
      penthouse: {
        blockJSRequests: false,
      }
    }), */
    new webpack.HotModuleReplacementPlugin()
  ]),

  resolve: {
    mainFields: ['jsnext:main', 'module', 'browser', 'main'],
    aliasFields: ['browser'],
    alias:      {
      underscore:                   'lodash',
      // because babel-loader doesn't work with "custom polyfills":
      'core-js/library/fn/promise': 'es6-promise',

      // optimize some dependencies
      debug:                    path.resolve(__dirname, '../node_modules/debug'),
      inherits:                 path.resolve(__dirname, '../node_modules/inherits'),
      argsarray:                path.resolve(__dirname, 'fixes/argsToArray'),
      'offline-plugin/runtime': 'null-loader',
      isarray:                  'lodash/isArray',

      // stable access to our main folder
      components: path.resolve(__dirname, '../src/components/'),
      pages:      path.resolve(__dirname, '../src/pages/'),
      styles:     path.resolve(__dirname, '../src/styles/'),

      // don't use es6, because transform-pouch cannot handle .default
      'pouchdb-promise': 'es6-promise',
      lie:               'es6-promise',
      'pouchdb-utils':   'pouchdb-utils/lib/index',

      // es6 rewrites
      'vue-multiselect': 'vue-multiselect/src/Multiselect'
    },
    extensions: ['.js', '.json', '.vue']
  }
};
