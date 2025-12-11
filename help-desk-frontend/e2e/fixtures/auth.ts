import { Page } from '@playwright/test';

export interface Credentials {
  email: string;
  password: string;
}

export const testUsers = {
  admin: {
    email: 'admin@admin.net',
    password: '123456',
  },
  technician: {
    email: 'mariana@tecnico.net',
    password: '123456',
  },
  user: {
    email: 'usuario@teste.net',
    password: '123456',
  },
  manager: {
    email: 'sonia.lima@gestor.net',
    password: '1234546',
  },
};

/**
 * Helper function to perform login
 */
export async function login(page: Page, credentials: Credentials) {
  await page.goto('/login', { timeout: 60000 });

  // Wait for login form to be visible
  await page.waitForSelector('input[type="email"]', { state: 'visible', timeout: 60000 });

  // Focus on email field to remove readonly attribute
  await page.click('input[type="email"]');

  // Fill email field
  await page.fill('input[type="email"]', credentials.email);

  // Focus on password field to remove readonly attribute
  await page.click('input[type="password"]');

  // Fill password field
  await page.fill('input[type="password"]', credentials.password);

  // Submit form
  await page.click('button[type="submit"]');

  // Wait for navigation or authentication to complete
  await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 30000 });

  // Verify sessionStorage has token (frontend uses sessionStorage not localStorage)
  const token = await page.evaluate(() => sessionStorage.getItem('token'));
  if (!token) {
    throw new Error('Login failed: No token found in sessionStorage');
  }
}

/**
 * Helper function to logout
 */
export async function logout(page: Page) {
  // Try multiple selectors for logout
  const logoutSelectors = [
    '[data-testid="user-menu"]',
    'a[href*="logout"]',
    'button:has-text("Sair")',
    'a:has-text("Sair")',
  ];

  let clicked = false;
  for (const selector of logoutSelectors) {
    try {
      await page.click(selector, { timeout: 2000 });
      clicked = true;
      break;
    } catch {
      continue;
    }
  }

  if (!clicked) {
    // If no logout button found, clear storage manually and go to login
    await page.evaluate(() => {
      sessionStorage.clear();
      localStorage.clear();
    });
    await page.goto('/login');
    return;
  }

  // Wait for redirect to login page
  await page.waitForURL('/login', { timeout: 5000 }).catch(() => {
    // If redirect doesn't happen, navigate manually
    return page.goto('/login');
  });

  // Wait a bit for storage cleanup
  await page.waitForTimeout(500);

  // Verify sessionStorage is cleared
  const token = await page.evaluate(() => sessionStorage.getItem('token'));
  if (token) {
    // Force clear if still present
    await page.evaluate(() => {
      sessionStorage.clear();
      localStorage.clear();
    });
  }
}

/**
 * Setup authenticated state for a test
 */
export async function setupAuthenticatedContext(page: Page, role: 'admin' | 'technician' | 'user' = 'user') {
  const credentials = testUsers[role];
  await login(page, credentials);
}
