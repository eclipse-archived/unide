/*
 * example file on how to build executable from /dist folder
 * install nw-builder first, than
 * node conf\build-nw.js
 */

const NwBuilder = require('nw-builder'),
      pkgJson   = require('../package.json'),
      nw        = new NwBuilder({
                  files:            [`${__dirname}/../dist/**`, `!../dist/${pkgJson.name}`],
                  platforms:        ['win64', 'linux64'], // ['win', 'win32', 'win64', 'osx', 'osx32', 'osx64', 'linux', 'linux32', 'linux64']
                  version:          '0.26.6',
                  flavor:           'normal',
                  appName:          pkgJson.name,
                  appVersion:       pkgJson.version,
                  buildDir:         `${__dirname}/../dist`,
                  cacheDir:         `${__dirname}/cache`,
                  winVersionString: {
                    CompanyName:      "Bosch Software Innovations",
                    FileDescription:  pkgJson.name,
                    ProductName:      pkgJson.name,
                    LegalCopyright:   "Copyright 2017"
                  },
                  winIco:           `${__dirname}/../src/img/icons/favicon.ico`
                });

nw.on('log',  console.log);

nw.build().then(function () {
   console.log('build');
}).catch(function (error) {
    console.error(error);
});
