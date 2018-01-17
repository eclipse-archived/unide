// var checker = require('license-checker'),
const fs    = require('fs'),
      path  = require('path'),
      util  = require('util'),
      JsZip = require('jszip');

class LicenseCheckPlugin {
  static parseRepository(repository) {
    if(typeof repository === 'object' && typeof repository.url === 'string') {
      return repository.url
        .replace('git+ssh://git@', 'git://')
        .replace('git+https://github.com', 'https://github.com')
        .replace('git://github.com', 'https://github.com')
        .replace('git@github.com:', 'https://github.com/')
        .replace('.git', '');
    }
    return '';
  }

  static parseLicense(license) {
    if(license instanceof Array) {
      return license.map(LicenseCheckPlugin.parseLicense).join(', ');
    } else if(typeof license === 'object' && typeof license.type) {
      return license.type;
    }
    return license;
  }

  apply(compiler) {
    compiler.plugin('emit', (compilation, callback) => {
      // last chance to add assets
      const sep                 = path.sep.replace(/\\/, '\\\\'),
            pkgName             = new RegExp(`(^.*node_modules${sep}[^${sep}]+)(?!.*node_modules)`),
            licenseRe           = /li[cs]ense/i,
            groupedDependencies = compilation.fileDependencies
              .reduce((l, d) => {
                const match = pkgName.exec(d);
                if(match) {
                  if(!l[match[1]]) {
                    l[match[1]] = [];
                  }
                  l[match[1]].push(d);
                }
                return l;
              }, {});

      Promise.all(Object.keys(groupedDependencies).map(module =>
        util.promisify(fs.readFile)(path.join(module, 'package.json'))
          .then(pgkJsonData => {
            const zip = new JsZip(),
                  pkg = JSON.parse(pgkJsonData);
            return Promise.all(groupedDependencies[module].map(file =>
              util.promisify(fs.readFile)(file)
                .then(data => zip.file(file.substring(module.length + 1), data))
            )
              // + try to find and add license text
              .concat(util.promisify(fs.readdir)(module)
                .then(list => {
                  const fileName = list.find(f => f.match(licenseRe));
                  if(fileName) {
                    return util.promisify(fs.readFile)(path.join(module, fileName))
                      .then(data => zip.file(fileName, data));
                  }
                  return Promise.resolve();
                })
              ))
              .then(() => zip.generateAsync({
                type: 'nodebuffer'
              }))
              .then(zipbuffer => {
                compilation.assets[`src/${pkg.name}@${pkg.version}.zip`] = {
                  source: () => zipbuffer,
                  size:   () => zipbuffer.length
                };
                return pkg;
              });
          })
      ))
        .then(PkgJsons => {
          const text = JSON.stringify(PkgJsons
            .sort((pkgA, pkgB) => (pkgA.name < pkgB.name) ? -1 : (pkgA.name > pkgB.name ? 1 : 0))
            .reduce((pkgs, pkg) => {
              pkgs[`${pkg.name}@${pkg.version}`] = {
                name:        pkg.name,
                version:     pkg.version,
                licenses:    LicenseCheckPlugin.parseLicense(pkg.license || pkg.licenses),
                repository:  LicenseCheckPlugin.parseRepository(pkg.repository),
                description: pkg.description
              };
              return pkgs;
            }, {})/*, ' ', 2 */);
          compilation.assets['licenses.json'] = {
            source: () => text,
            size:   () => Buffer.byteLength(text, 'utf8')
          };
          return callback();
        })
        .catch(err => {
          console.error(err);
          callback();
        });
    });

    /* compiler.plugin('emit', (compilation, done) => {
      checker.init({
        start: `${__dirname}/..`,
        production: true,
        json: true,
        customFormat: {
          name: true,
          version: true,
          description: true,
          repository: true,
          licenses: true,

          publisher: false,
          email: false,
          url: false,
          licenseFile: false,
          licenseText: false,
          licenseModified: false
        }
      }, function(err, json) {
        if(err) {
          console.error(err);
        } else {
          let text = JSON.stringify(json, ' ', 2);
          compilation.assets['licenses.json'] = {
            source: () => text,
            size: () => Buffer.byteLength(text, 'utf8')
          };
        }
        done();
      });
    }); */
  }
}

module.exports = LicenseCheckPlugin;
