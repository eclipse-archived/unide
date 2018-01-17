const webpack                         = require('webpack'),
      path                            = require('path'),
      fs                              = require('fs'),
      ExtractTextPlugin               = require('extract-text-webpack-plugin'),
      LodashModuleReplacementPlugin   = require('lodash-webpack-plugin'),
      CopyWebpackPlugin               = require('copy-webpack-plugin'),
      OfflinePlugin                   = require('offline-plugin'),
      HtmlWebpackPlugin               = require('html-webpack-plugin'),
      CleanPlugin                     = require('clean-webpack-plugin'),
      UglifyJsPlugin                  = require('uglifyjs-webpack-plugin'),
    // FaviconsWebpackPlugin          = require('favicons-webpack-plugin'),
      LicensePlugin                   = require('./webpack-license-plugin'),
    // HtmlCriticalPlugin              = require('html-critical-webpack-plugin'),
      packageJson                     = require('../package.json'),

      extractCSS  = new ExtractTextPlugin({
        filename: '[name].css'
      }),
      compact     = true,
      sassLoader  = {
        loader:  'sass-loader',
        options: {
          sourceMap:    !compact,
          outputStyle:  'compressed',
          includePaths: ['node_modules']
        }
      },
      langs       = fs.readdirSync(`${__dirname}/../src/i18n`).filter(f => f.endsWith('.json')).map(file => file.replace(/.json$/, '')),
      publicPath  = process.env.PREFIX || '/binsa/';

module.exports = {
//  devtool: 'hidden-source-map',
  entry:   {
    app:     './src',
    sandbox: './src/sandbox'
  },

  output: {
    path:           path.join(__dirname, '../dist', publicPath),
    filename:       '[name].js',
    chunkFilename:  '[name].js',
    pathinfo:       true,
    publicPath:     publicPath
  },
 	
  target: "web",
  
  node: {
    // node polyfills
  },

  module: {
    rules: [{
      test:    /\.vue$/i,
      loader:  'vue-loader',
      options: {
        loaders: {
          js:  'babel-loader',
          css: extractCSS.extract({
            fallback: 'vue-style-loader',
            use:      [{
              loader:  'css-loader',
              options: {
                sourceMap: !compact,
                minimize:  compact
              }
            }]
          }),
          scss: extractCSS.extract({
            fallback: 'vue-style-loader',
            use:      [{
              loader:  'css-loader',
              options: {
                sourceMap: !compact,
                minimize:  compact
              }
            }, sassLoader]
          }),
          sass: extractCSS.extract({
            fallback: 'vue-style-loader',
            use:      [{
              loader:  'css-loader',
              options: {
                sourceMap: !compact,
                minimize:  compact
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
      include: [
        path.resolve(__dirname, '..', 'src'),
        path.resolve(__dirname, '..', 'conf', 'fixes'),
        path.resolve(__dirname, '..', 'node_modules', 'buefy'),
        path.resolve(__dirname, '..', 'node_modules', 'native-crypto', 'browser')
      ],
      use:     ['babel-loader', 'eslint-loader']
    }, {
      test: /\.css$/i,
      use:  extractCSS.extract({
        fallback: 'style-loader',
        use:      {
          loader:  'css-loader',
          options: {
            sourceMap: !compact,
            minimize:  compact
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
          limit: 10000,
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
    packageJson: packageJson,
    minimize:    compact
  })).concat([
    new LicensePlugin(),
    // new DashboardPlugin(),
    new CleanPlugin(['dist'], {
      root: path.resolve(__dirname, '..')
      //, exclude: ["i18n/en.json"]
    }),
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
      NODE_ENV: 'production',
      BASEPATH: publicPath,
      LANGS:    langs,
      BRAND:    packageJson.name
    }),
    //limit the locales of vee-validate to the ones we know 
    new webpack.ContextReplacementPlugin(/vee-validate[\/\\]dist[\/\\]locale$/, new RegExp(langs.join('|'))),
    new LodashModuleReplacementPlugin({
      collection: true,
      path:       true
    }),
    new UglifyJsPlugin({
      sourceMap: !compact,
      uglifyOptions: {
        compress: compact ? {
          drop_console: true
        } : false,
        mangle: compact,
        sourceMap: !compact
      }
    }),
    new webpack.LoaderOptionsPlugin({
      minimize: compact
    }),
    new webpack.optimize.CommonsChunkPlugin({
      minChunks: 2, //module => module.context && (module.context.indexOf('node_modules') !== -1),
      children: true,
      deepChildren: true
    }),
    new OfflinePlugin({
      publicPath:               publicPath,
      excludes:                 ['**/.*', '**/*.map', '**/*.zip', '**/fontawesome-webfont.+(svg|eot|ttf|woff)'],
      safeToUseOptionalCaches:  true,
      caches:                   {
        main:       ['app.js', 'app.css', 'logo.svg', 'fontawesome-webfont.woff2', '**/favicon-16x16.png', '**/favicon-32x32.png'],
        additional: ['**/*.js', '**/*.json'],
        optional:   [':rest:']
      },
      ServiceWorker:            {
        entry:  './src/sw.js',
        events: true,
        navigateFallbackURL: publicPath
        //, autoUpdate: 1000 * 60 * 60
      }
    }),
    /* new HtmlCriticalPlugin({
      base:     `${__dirname}/../dist`,
      src:      `${publicPath}index.html`,
      dest:     `${publicPath}index.html`,
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
        renderWaitTime:  100 
      }
    }) */
  ]),

  resolve: {
    mainFields: ['jsnext:main', 'module', 'browser', 'main'],
    aliasFields: ['browser'],
    alias: {
      underscore: 'lodash',
      // because babel-loader doesn't work with "custom polyfills":
      "core-js/library/fn/promise": "es6-promise",

      // optimize some dependencies
      debug:      path.resolve(__dirname, '../node_modules/debug'),
      inherits:   path.resolve(__dirname, '../node_modules/inherits'),
      argsarray:  path.resolve(__dirname, 'fixes/argsToArray'),
      isarray:    'lodash/isArray',

      // stable access to our main folders
      components: path.resolve(__dirname, '../src/components/'),
      pages:      path.resolve(__dirname, '../src/pages/'),
      styles:     path.resolve(__dirname, '../src/styles/'),

      // don't use es6, because transform-pouch cannot handle .default
      'pouchdb-promise':  'es6-promise',
      'lie':              'es6-promise',
      'pouchdb-utils':    'pouchdb-utils/lib/index'
    },
    extensions: ['.js', '.json', '.vue']
  }
};

