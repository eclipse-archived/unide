const fs                  = require('fs'),
      VirtualModulePlugin = require('webpack-virtual-modules'),
      fm                  = require('front-matter'),
      marked              = new (require('markdown-it'))({
        html:    true,
        linkify: true,
        breaks:  true,
        plugins: [
          require('markdown-it-decorate')
        ]
      }),
      path                = require('path'),
      postFiles           = fs.readdirSync(path.join(__dirname, '..', 'blog'))
        .filter(filename => filename.endsWith('.md'))
        .map(filename => {
          const filepath = path.join(__dirname, '..', 'blog', filename),
                data     = fs.readFileSync(filepath),
                {
                  attributes,
                  body
                } = fm(data.toString()),
                name = `${filename.substr(0, filename.length - 3)}`;
          if(attributes.date) {
            attributes.date = new Date(attributes.date);
          } else {
            return null;
          }
          if(attributes.tags) {
            if(!(attributes.tags instanceof Array)) {
              attributes.tags = [attributes.tags];
            }
          } else {
            attributes.tags = [];
          }
          return Object.assign({
            name,
            content: marked.render(body),
            url:     attributes.url || (attributes.date && `/blog/${attributes.date.getFullYear()}/${attributes.date.getMonth() + 1}/${attributes.date.getDate()}/${name}`)
          }, attributes);
        }).filter(f => f).sort((a, b) => a.date - b.date);

postFiles.forEach((file, idx) => {
  if(idx) {
    const prev = postFiles[idx - 1];
    prev.next = {
      url:   file.url,
      title: file.title
    };
    file.prev = {
      url:   prev.url,
      title: prev.title
    };
  }
});

module.exports = function(options) {
  this.options.build = this.options.build || {};
  const extend       = this.options.build.extend,
        postStructure = postFiles.reduce((l, file) => {
          const f = {
            url:   file.url,
            title: file.title,
            date:  file.date
          };
          if(!f.date) {
            return l;
          }
          // eslint-disable-next-line one-var
          const year  = f.date.getFullYear(),
                month = f.date.getMonth() + 1,
                day   = f.date.getDate();
          if(!l.hierarchy[year]) {
            l.hierarchy[year] = {};
            l.list[`${year}`] = [];
          }
          if(!l.hierarchy[year][month]) {
            l.hierarchy[year][month] = {};
            l.list[`${year}/${month}`] = [];
          }
          if(!l.hierarchy[year][month][day]) {
            l.hierarchy[year][month][day] = 0;
            l.list[`${year}/${month}/${day}`] = [];
          }

          l.list[`${year}`].push(f);
          l.list[`${year}/${month}`].push(f);
          l.list[`${year}/${month}/${day}`].push(f);
          l.hierarchy[year][month][day]++;
          return l;
        }, {
          hierarchy: {},
          list:      {}
        });

  // render all the posts
  postFiles.forEach((file, idx) => {
    if(file.url) {
      this.options.generate.routes.push({
        route:   file.url,
        payload: file
      });
    }
  });

  // render the year/month/day indexes
  Object.entries(postStructure.list).forEach(([postPath, list]) => {
    this.options.generate.routes.push({
      route:   `/blog/${postPath}`,
      payload: list
    });
  });

  // don't extend routes for every post as this is part of every (spa) page (router config)
  this.options.router = this.options.router || {};
  this.options.router.extendRoutes = (routes, resolve) => {
    routes.push({
      path:      `/blog/:year/:month/:day/:name`,
      component: resolve(path.join(__dirname, '..', 'pages', 'article.vue'))
    }, {
      path:      `/blog/:year?/:month?/:day?`,
      component: resolve(path.join(__dirname, '..', 'pages', 'blog.vue'))
    });
  };

  this.options.build.extend = function(config, ctx) {
    // virtual pages
    config.plugins.push(
      new VirtualModulePlugin({
        'node_modules/posts/recent.json': JSON.stringify(postFiles.slice(-5).map(file => ({
          url:   file.url,
          title: file.title,
          date:  file.date
        }))),
        'node_modules/posts/archives.json': JSON.stringify(Object.entries(postStructure.hierarchy).reduce((l, [year, months]) => {
          Object.entries(months).forEach(([month, days]) => {
            l.push({
              year:  year,
              month: month,
              url:   `/blog/${year}/${month}`,
              count: Object.values(days).reduce((count, day) => {
                count += day;
                return count;
              }, 0)
            });
          });
          return l;
        }, []))
      })
    );

    config.plugins.push({
      apply(compiler) {
        compiler.plugin('emit', (compilation, callback) => {
          postFiles.forEach(file => {
            const str = JSON.stringify(file);
            compilation.assets[`posts/${file.name}.json`] = {
              source: () => str,
              size:   () => str.length
            };
          });
          Object.entries(postStructure.list).forEach(([postPath, list]) => {
            const str = JSON.stringify(list);
            compilation.assets[`posts/${postPath}.json`] = {
              source: () => str,
              size:   () => str.length
            };
          });
          return callback();
        });
      }
    });

    return extend.apply(this, arguments);
  };
};
