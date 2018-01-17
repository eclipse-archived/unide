// console.log('own sw.js', self);

const API_CACHE_NAME = 'ppm-api-cache';

/*
function useFallback() {
  return Promise.resolve(new Response('errortext', {
    headers: {
      'Content-Type': 'image/svg+xml'
    }
  }));
}
*/

self.addEventListener('fetch', event => {
  // cache PPM REST API
  if(event.request.method === 'GET' && event.request.url.search(/INL_CY/) !== -1) {
    event.respondWith(caches.open(API_CACHE_NAME).then(function(cache) {
      return fetch(event.request)
        .then(function(response) {
          if(!response.ok) {
            return Promise.reject(response);
          }
          return cache.put(event.request, response.clone())
            .then(function() {
              return response;
            });
        })
        .catch(function(err) {
          return cache.match(event.request)
            .then(function(matching) {
              return matching || Promise.reject(err);
            });
        });
    }));
  }
});
