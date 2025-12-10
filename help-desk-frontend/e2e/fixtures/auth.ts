import { Page } from '@playwright/test';

export interface Credentials {
  email: string;
  password: string;
}

export const testUsers = {
  admin: {
    email: 'admin@helpdesk.com',
    password: 'admin123',
  },
  technician: {
    email: 'tecnico@helpdesk.com',
    password: 'tecnico123',
  },
  user: {
    email: 'user@helpdesk.com',
    password: 'user123',
  },
};

/**
 * Helper function to perform login
 */
export async function login(page: Page, credentials: Credentials) {
  await page.goto('/login');

  // Wait for login form to be visible
  await page.waitForSelector('input[type="email"]', { state: 'visible' });

  // Fill login form
  await page.fill('input[type="email"]', credentials.email);
  await page.fill('input[type="password"]', credentials.password);

  // Submit form
  await page.click('button[type="submit"]');

  // Wait for navigation or authentication to complete
  await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 10000 });

  // Verify localStorage has token
  const token = await page.evaluate(() => localStorage.getItem('token'));
  if (!token) {
    throw new Error('Login failed: No token found in localStorage');
  }
}

/**
 * Helper function to logout
 */
export async function logout(page: Page) {
  // Click on user menu or logout button
  await page.click('[data-testid="user-menu"]').catch(() => {
    // Fallback: try to find logout link
    return page.click('a[href*="logout"]');
  });

  // Wait for redirect to login page
  await page.waitForURL('/login', { timeout: 5000 });

  // Verify localStorage is cleared
  const token = await page.evaluate(() => localStorage.getItem('token'));
  if (token) {
    throw new Error('Logout failed: Token still present in localStorage');
  }
}

/**
 * Setup authenticated state for a test
 */
export async function setupAuthenticatedContext(page: Page, role: 'admin' | 'technician' | 'user' = 'user') {
  const credentials = testUsers[role];
  await login(page, credentials);
}
