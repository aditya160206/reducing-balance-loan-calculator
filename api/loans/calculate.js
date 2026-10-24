export default async function handler(request, response) {
  if (request.method !== 'POST') {
    response.setHeader('Allow', 'POST');
    return response.status(405).json({ error: 'Method not allowed. Use POST.' });
  }

  const backendUrl = process.env.BACKEND_URL;
  if (!backendUrl) {
    return response.status(500).json({ error: 'The calculator backend is not configured.' });
  }

  try {
    const upstream = await fetch(new URL('/api/loans/calculate', backendUrl), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request.body)
    });
    const body = await upstream.text();

    response.status(upstream.status);
    response.setHeader(
      'Content-Type',
      upstream.headers.get('content-type') || 'application/json'
    );
    return response.send(body);
  } catch (error) {
    console.error('Unable to reach the loan calculation backend:', error);
    return response.status(502).json({ error: 'The calculator backend is unavailable.' });
  }
}
