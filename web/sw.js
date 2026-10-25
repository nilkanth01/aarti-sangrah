/* Offline support. __BUILD_ID__ is replaced on every deploy, so a new version installs itself. */
const BUILD = '__BUILD_ID__';
const SHELL = `aarti-shell-${BUILD}`;
const DATA = 'aarti-data';
const SHELL_FILES = [
  './',
  'index.html',
  `app.css?v=${BUILD}`,
  `app.js?v=${BUILD}`,
  'manifest.webmanifest',
  'icons/diya.svg',
  'icons/favicon.svg',
  'icons/apple-touch-icon.png',
  'icons/icon-192.png',
  'icons/icon-512.png',
  'fonts/mukta_regular.ttf',
  'fonts/mukta_semibold.ttf',
  'fonts/mukta_bold.ttf',
  'fonts/tiro_marathi.ttf',
];

// cache: 'reload' skips the browser's HTTP cache so a new deploy never stores old files.
const fresh = url => new Request(url, { cache: 'reload' });

self.addEventListener('install', event => {
  event.waitUntil(
    Promise.all([
      caches.open(SHELL).then(cache => cache.addAll(SHELL_FILES.map(fresh))),
      caches.open(DATA).then(cache => cache.add(fresh('content.json'))),
    ]).then(() => self.skipWaiting()),
  );
});

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys()
      .then(keys => Promise.all(keys.filter(k => k.startsWith('aarti-shell-') && k !== SHELL).map(k => caches.delete(k))))
      .then(() => self.clients.claim()),
  );
});

/** content.json: always try the network first so new aartis show up, fall back to the saved copy offline. */
async function contentFirstFromNetwork(request) {
  const cache = await caches.open(DATA);
  try {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 5000);
    const response = await fetch(request, { cache: 'no-store', signal: controller.signal });
    clearTimeout(timer);
    if (response.ok) await cache.put('content.json', response.clone());
    return response;
  } catch {
    return (await cache.match('content.json')) || Response.error();
  }
}

self.addEventListener('fetch', event => {
  const url = new URL(event.request.url);
  if (event.request.method !== 'GET' || url.origin !== self.location.origin) return;

  if (url.pathname.endsWith('/content.json')) {
    event.respondWith(contentFirstFromNetwork(event.request));
    return;
  }
  if (event.request.mode === 'navigate') {
    event.respondWith(
      caches.match('index.html').then(cached => cached || fetch(event.request)).catch(() => caches.match('./')),
    );
    return;
  }
  event.respondWith(caches.match(event.request).then(cached => cached || fetch(event.request)));
});
