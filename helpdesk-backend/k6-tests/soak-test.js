import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

// Soak test - sustained load over long period
export const options = {
  stages: [
    { duration: '2m', target: 50 },   // Ramp up
    { duration: '30m', target: 50 },  // Sustained load (30 minutes)
    { duration: '2m', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
    errors: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_VERSION = '/v1';

export function setup() {
  const loginRes = http.post(
    `${BASE_URL}${API_VERSION}/api/auth/login`,
    JSON.stringify({
      email: 'loadtest@helpdesk.com',
      password: 'LoadTest123!',
    }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  if (loginRes.status === 200) {
    return { token: JSON.parse(loginRes.body).token };
  }
  return { token: null };
}

export default function (data) {
  if (!data.token) return;

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${data.token}`,
  };

  // Realistic user behavior
  http.get(`${BASE_URL}${API_VERSION}/api/tickets`, { headers });
  sleep(2);

  http.get(`${BASE_URL}${API_VERSION}/api/categorias`, { headers });
  sleep(1);

  http.get(`${BASE_URL}${API_VERSION}/api/prioridades`, { headers });
  sleep(1);

  // Occasionally create a ticket
  if (Math.random() < 0.1) {
    const newTicket = {
      titulo: `Soak Test Ticket ${Date.now()}`,
      descricao: 'Testing system stability over time',
      categoriaId: 1,
      prioridadeId: 2,
    };

    const res = http.post(
      `${BASE_URL}${API_VERSION}/api/tickets`,
      JSON.stringify(newTicket),
      { headers }
    );

    check(res, {
      'create ticket success': (r) => r.status === 201,
    }) || errorRate.add(1);

    sleep(3);
  }

  sleep(5); // Simulate user reading/thinking time
}
