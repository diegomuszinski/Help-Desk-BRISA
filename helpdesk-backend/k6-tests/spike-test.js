import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

// Spike test - sudden increase in load
export const options = {
  stages: [
    { duration: '10s', target: 10 },    // Normal load
    { duration: '5s', target: 500 },    // Sudden spike!
    { duration: '30s', target: 500 },   // Sustain spike
    { duration: '10s', target: 10 },    // Recovery
    { duration: '30s', target: 10 },    // Normal load
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // More lenient during spike
    http_req_failed: ['rate<0.05'],     // 5% error rate acceptable
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

  // Simple read operation to test system under spike
  const res = http.get(`${BASE_URL}${API_VERSION}/api/tickets`, { headers });
  
  check(res, {
    'status 200 or 429': (r) => r.status === 200 || r.status === 429, // Rate limiting expected
    'response time acceptable': (r) => r.timings.duration < 5000,
  }) || errorRate.add(1);

  sleep(0.5); // Minimal sleep for maximum load
}
