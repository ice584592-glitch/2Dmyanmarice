const express = require('express');

const app = express();
const port = process.env.PORT || 3000;
const upstream = 'https://setlive.myanmarnode.com';
const apiKey = process.env.SETLIVE_API_KEY;

if (!apiKey) {
  console.error('Missing SETLIVE_API_KEY environment variable');
  process.exit(1);
}

app.get('/health', (_req, res) => res.json({ ok: true }));

// Only proxy the documented endpoints; never accept an arbitrary upstream URL.
function allowedPath(path) {
  return path === '/api/v1/twod-result-live' ||
    path === '/api/v1/twod-results' ||
    path === '/api/v1/twod-ticks' ||
    path === '/api/v1/twod-result-stream' ||
    path === '/api/v1/threed-result-latest' ||
    path === '/api/v1/threed-results' ||
    /^\/api\/v1\/twod-ticks\/\d{2}$/.test(path) ||
    /^\/api\/v1\/threed-results\/\d{4}-\d{2}-\d{2}$/.test(path);
}

app.get('/api/v1/*', async (req, res) => {
  const path = req.path;
  if (!allowedPath(path)) return res.status(404).json({ message: 'Endpoint not allowed' });

  const url = new URL(upstream + path);
  for (const [key, value] of Object.entries(req.query)) {
    if (typeof value === 'string') url.searchParams.set(key, value);
  }

  try {
    const response = await fetch(url, {
      headers: {
        'X-API-Key': apiKey,
        'Accept': path.endsWith('twod-result-stream') ? 'text/event-stream' : 'application/json'
      },
      signal: AbortSignal.timeout(60000)
    });
    res.status(response.status);
    res.set('Content-Type', response.headers.get('content-type') || 'application/json');
    res.set('Cache-Control', 'no-store');
    if (!response.body) return res.end();
    if (path.endsWith('twod-result-stream')) {
      res.set('Connection', 'keep-alive');
      for await (const chunk of response.body) res.write(chunk);
      return res.end();
    }
    res.send(Buffer.from(await response.arrayBuffer()));
  } catch (error) {
    if (!res.headersSent) res.status(502).json({ message: 'Upstream API unavailable' });
    else res.end();
  }
});

app.listen(port, () => console.log(`API proxy listening on ${port}`));
