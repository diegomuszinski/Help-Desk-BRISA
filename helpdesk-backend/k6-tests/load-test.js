import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');

// Test configuration
export const options = {
  stages: [
    { duration: '30s', target: 10 },   // Ramp up to 10 users
    { duration: '1m', target: 10 },    // Stay at 10 users
    { duration: '30s', target: 50 },   // Ramp up to 50 users
    { duration: '2m', target: 50 },    // Stay at 50 users
    { duration: '30s', target: 100 },  // Ramp up to 100 users
    { duration: '2m', target: 100 },   // Stay at 100 users
    { duration: '30s', target: 0 },    // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'], // 95% requests < 500ms, 99% < 1s
    http_req_failed: ['rate<0.01'],                  // Error rate < 1%
    errors: ['rate<0.1'],                            // Custom error rate < 10%
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_VERSION = '/v1';

// Test data
const testUser = {
  email: 'loadtest@helpdesk.com',
  password: 'LoadTest123!',
};

let authToken = null;

export function setup() {
  // Login to get auth token
  const loginRes = http.post(
    `${BASE_URL}${API_VERSION}/api/auth/login`,
    JSON.stringify(testUser),
    {
      headers: { 'Content-Type': 'application/json' },
    }
  );

  if (loginRes.status === 200) {
    const body = JSON.parse(loginRes.body);
    return { token: body.token };
  }
  
  console.error('Setup failed: Unable to login');
  return { token: null };
}

export default function (data) {
  if (!data.token) {
    console.error('No auth token available');
    return;
  }

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${data.token}`,
  };

  // Test 1: List tickets
  const listRes = http.get(`${BASE_URL}${API_VERSION}/api/tickets`, { headers });
  check(listRes, {
    'list tickets status 200': (r) => r.status === 200,
    'list tickets duration < 200ms': (r) => r.timings.duration < 200,
  }) || errorRate.add(1);

  sleep(1);

  // Test 2: Get categories
  const categoriesRes = http.get(`${BASE_URL}${API_VERSION}/api/categorias`, { headers });
  check(categoriesRes, {
    'get categories status 200': (r) => r.status === 200,
    'get categories cached': (r) => r.timings.duration < 50, // Should be cached
  }) || errorRate.add(1);

  sleep(1);

  // Test 3: Get priorities
  const prioritiesRes = http.get(`${BASE_URL}${API_VERSION}/api/prioridades`, { headers });
  check(prioritiesRes, {
    'get priorities status 200': (r) => r.status === 200,
    'get priorities cached': (r) => r.timings.duration < 50, // Should be cached
  }) || errorRate.add(1);

  sleep(1);

  // Test 4: Create ticket (simulate user activity)
  const newTicket = {
    titulo: `Load Test Ticket ${Date.now()}`,
    descricao: 'This is a load test ticket created by K6',
    categoriaId: 1,
    prioridadeId: 2,
  };

  const createRes = http.post(
    `${BASE_URL}${API_VERSION}/api/tickets`,
    JSON.stringify(newTicket),
    { headers }
  );
  
  check(createRes, {
    'create ticket status 201': (r) => r.status === 201,
    'create ticket duration < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);

  sleep(2);
}

export function teardown(data) {
  console.log('Load test completed');
}
