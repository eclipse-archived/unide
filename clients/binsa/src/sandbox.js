window.addEventListener('message', function(ev) {
  const id = ev.data.id;
  try {
    const testdata    = event.data || {},
          tasks       = [],
          render      = function(tpl, data) {
            return Promise.resolve(data);
          };
    if(typeof testdata.data === 'string') {
      /* eslint no-eval: 0 */
      eval("'use scrict;'; testdata.data=" + testdata.data);
    }
    testdata.times = testdata.times || 1;

    for(let i = 0; i < testdata.times; i++) {
      tasks.push(render(testdata.tpl, testdata.data));
    }
    Promise.all(tasks)
      .then(function(result) {
        ev.source.postMessage({ id: id, result: result }, '*');
        return result;
      })
      .catch(function(err) {
        ev.source.postMessage({ id: id, error: err.toString() }, '*');
      });
  } catch(err) {
    ev.source.postMessage({ id: id, error: err.toString() }, '*');
  }
});
