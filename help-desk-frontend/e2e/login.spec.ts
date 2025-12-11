import { test, expect } from '@playwright/test';
import { testUsers, login, logout } from './fixtures/auth';

test.describe('Authentication Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Clear localStorage before each test
    await page.goto('/');
    await page.evaluate(() => localStorage.clear());
  });

  test('should display login page', async ({ page }) => {
    await page.goto('/login');

    // Check if login form is visible
    await expect(page.locator('input[type="email"]')).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
  });

  test('should login with valid admin credentials', async ({ page }) => {
    await login(page, testUsers.admin);

    // Verify redirect to dashboard, home, or fila
    await expect(page).toHaveURL(/\/(dashboard|home|fila)/);

    // Verify token in sessionStorage
    const token = await page.evaluate(() => sessionStorage.getItem('token'));
    expect(token).toBeTruthy();
  });

  test('should login with valid technician credentials', async ({ page }) => {
    await login(page, testUsers.technician);

    // Verify successful login - technicians go to /fila
    await expect(page).toHaveURL(/\/(dashboard|home|tickets|fila)/);

    const token = await page.evaluate(() => sessionStorage.getItem('token'));
    expect(token).toBeTruthy();
  });

  test('should login with valid user credentials', async ({ page }) => {
    await login(page, testUsers.user);

    // Verify successful login - just check not on login page
    await expect(page).not.toHaveURL('/login');

    const token = await page.evaluate(() => sessionStorage.getItem('token'));
    expect(token).toBeTruthy();
  });

  test('should show error with invalid credentials', async ({ page }) => {
    await page.goto('/login');

    // Click to remove readonly before filling
    await page.click('input[type="email"]');
    await page.fill('input[type="email"]', 'invalid@test.com');
    await page.click('input[type="password"]');
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');

    // Should stay on login page or show error message
    await page.waitForTimeout(2000);

    // Check for error message (adjust selector based on your UI)
    const errorVisible = await page.locator('.error, .alert-danger, [role="alert"]').isVisible().catch(() => false);
    const stillOnLoginPage = page.url().includes('/login');

    expect(errorVisible || stillOnLoginPage).toBeTruthy();
  });

  test('should logout successfully', async ({ page }) => {
    // Login first
    await login(page, testUsers.user);

    // Logout
    await logout(page);

    // Verify redirect to login page - accept /login or /fila routes (some apps redirect to queue)
    const currentUrl = page.url();
    const isRedirected = currentUrl.includes('/login') || currentUrl.includes('/fila');
    expect(isRedirected).toBeTruthy();

    // Verify token is removed
    const token = await page.evaluate(() => sessionStorage.getItem('token'));
    expect(token).toBeNull();
  });

  test('should redirect to login when accessing protected route without auth', async ({ page }) => {
    await page.goto('/dashboard');

    // Should redirect to login
    await page.waitForURL('/login', { timeout: 5000 });
    await expect(page).toHaveURL('/login');
  });

  test('should remember user session on page reload', async ({ page }) => {
    await login(page, testUsers.user);

    // Reload page
    await page.reload();

    // Should still be authenticated
    await page.waitForTimeout(1000);
    expect(page.url()).not.toContain('/login');

    const token = await page.evaluate(() => sessionStorage.getItem('token'));
    expect(token).toBeTruthy();
  });
});
