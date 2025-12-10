import { test, expect } from '@playwright/test';
import { setupAuthenticatedContext, testUsers } from './fixtures/auth';

test.describe('Rate Limiting', () => {
  test('should enforce rate limit on ticket creation', async ({ page }) => {
    await setupAuthenticatedContext(page, 'user');

    // Make multiple rapid requests to trigger rate limit
    const responses: number[] = [];

    for (let i = 0; i < 35; i++) {
      const response = await page.request.post('http://localhost:8080/api/tickets', {
        headers: {
          'Authorization': `Bearer ${await page.evaluate(() => localStorage.getItem('token'))}`,
          'Content-Type': 'application/json',
        },
        data: {
          titulo: `Rate Limit Test ${i}`,
          descricao: 'Testing rate limiting',
          categoriaId: 1,
          prioridadeId: 1,
        },
      }).catch(() => null);

      if (response) {
        responses.push(response.status());
      }
    }

    // Should have some 429 (Too Many Requests) responses
    const rateLimitedResponses = responses.filter(status => status === 429);
    expect(rateLimitedResponses.length).toBeGreaterThan(0);
  });

  test('should return rate limit headers', async ({ page }) => {
    await setupAuthenticatedContext(page, 'user');

    const response = await page.request.post('http://localhost:8080/api/tickets', {
      headers: {
        'Authorization': `Bearer ${await page.evaluate(() => localStorage.getItem('token'))}`,
        'Content-Type': 'application/json',
      },
      data: {
        titulo: 'Rate Limit Header Test',
        descricao: 'Testing rate limiting headers',
        categoriaId: 1,
        prioridadeId: 1,
      },
    }).catch(() => null);

    if (response) {
      const headers = response.headers();

      // Should have rate limit headers
      expect(headers['x-ratelimit-limit'] || headers['x-rate-limit-limit']).toBeDefined();
    }
  });

  test('should respect different rate limits per endpoint', async ({ page }) => {
    await setupAuthenticatedContext(page, 'user');

    const token = await page.evaluate(() => localStorage.getItem('token'));

    // Test POST /api/tickets (30 req/min per IP)
    const ticketResponses: number[] = [];
    for (let i = 0; i < 32; i++) {
      const response = await page.request.post('http://localhost:8080/api/tickets', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        data: {
          titulo: `Test ${i}`,
          descricao: 'Test',
          categoriaId: 1,
          prioridadeId: 1,
        },
      }).catch(() => null);

      if (response) ticketResponses.push(response.status());
    }

    // Should hit rate limit
    const ticketRateLimited = ticketResponses.filter(s => s === 429).length;
    expect(ticketRateLimited).toBeGreaterThan(0);
  });

  test('should reset rate limit after time window', async ({ page }) => {
    await setupAuthenticatedContext(page, 'user');

    const token = await page.evaluate(() => localStorage.getItem('token'));

    // Make requests to consume rate limit
    for (let i = 0; i < 31; i++) {
      await page.request.post('http://localhost:8080/api/tickets', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        data: {
          titulo: `Test ${i}`,
          descricao: 'Test',
          categoriaId: 1,
          prioridadeId: 1,
        },
      }).catch(() => null);
    }

    // Wait for rate limit window to reset (1 minute + buffer)
    await page.waitForTimeout(65000);

    // Should be able to make requests again
    const response = await page.request.post('http://localhost:8080/api/tickets', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      data: {
        titulo: 'After Reset Test',
        descricao: 'Test after rate limit reset',
        categoriaId: 1,
        prioridadeId: 1,
      },
    }).catch(() => null);

    // Should NOT be rate limited
    if (response) {
      expect(response.status()).not.toBe(429);
    }
  });
});
