const CACHE_NAME = 'sysred-v1';
const STATIC_ASSETS = [
    '/index.html',
    '/dist/css/adminlte.min.css',
    '/plugins/fontawesome-free/css/all.min.css',
    '/plugins/jquery/jquery.min.js',
    '/plugins/bootstrap/js/bootstrap.bundle.min.js',
    '/dist/js/adminlte.js',
    '/plugins/chart.js/Chart.min.js',
    '/js/api.js',
    '/js/utils.js',
    '/js/router.js',
    '/js/pages/home.js',
    '/js/pages/clientes.js',
    '/js/pages/facturas.js',
    '/js/pages/pagos.js',
    '/js/pages/planes.js'
];

self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME).then(cache => cache.addAll(STATIC_ASSETS))
    );
    self.skipWaiting();
});

self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys().then(keys =>
            Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
        )
    );
    self.clients.claim();
});

self.addEventListener('fetch', event => {
    if (event.request.method !== 'GET') return;

    // API calls: network first, no cache
    if (event.request.url.includes('/api/')) return;

    // Static assets: cache first, fallback to network
    event.respondWith(
        caches.match(event.request).then(cached => {
            return cached || fetch(event.request).then(response => {
                if (response.ok) {
                    const clone = response.clone();
                    caches.open(CACHE_NAME).then(cache => cache.put(event.request, clone));
                }
                return response;
            });
        }).catch(() => caches.match('/index.html'))
    );
});
