import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

// Stress test - gradually increase load until system breaks
export const options = {
  stages: [
    { duration: '1m', target: 50 },
    { duration: '2m', target: 100 },
    { duration: '2m', target: 200 },
    { duration: '2m', target: 300 },
    { duration: '2m', target: 400 },
    { duration: '5m', target: 500 }, // Breaking point
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<5000'],
    http_req_failed: ['rate<0.1'],
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

  // Mix of operations
  const operations = [
    () => http.get(`${BASE_URL}${API_VERSION}/api/tickets`, { headers }),
    () => http.get(`${BASE_URL}${API_VERSION}/api/categorias`, { headers }),
    () => http.get(`${BASE_URL}${API_VERSION}/api/prioridades`, { headers }),
  ];

  const randomOp = operations[Math.floor(Math.random() * operations.length)];
  const res = randomOp();

  check(res, {
    'status is 2xx or 429': (r) => (r.status >= 200 && r.status < 300) || r.status === 429,
  }) || errorRate.add(1);

  sleep(0.5);
}
