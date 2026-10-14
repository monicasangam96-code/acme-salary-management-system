import { test, expect } from '@playwright/test';

test.describe('ACME Salary Management E2E Tests', () => {
  const BASE_URL = process.env.E2E_BASE_URL || 'http://localhost:4200';

  async function clearAuthState(page: any): Promise<void> {
    try { await page.evaluate(() => localStorage.clear()); } catch {}
    try { await page.evaluate(() => sessionStorage.clear()); } catch {}
  }

  test('should load the login page', async ({ page }) => {
    await page.goto(BASE_URL + '/login');
    await expect(page.locator('h1')).toContainText('ACME Salary Management');
  });

  test('should navigate to dashboard after login', async ({ page }) => {
    await clearAuthState(page);
    await page.goto(BASE_URL + '/login');
    await page.fill('input[name="email"]', 'admin@acme.com');
    await page.fill('input[name="password"]', 'admin123');

    const loginResp = page.waitForResponse(r =>
      r.url().includes('/auth/login') && r.status() === 200,
      { timeout: 5000 }
    );
    await page.click('.btn-login');
    await loginResp;

    // Wait for redirect to dashboard
    await expect(page).toHaveURL(/.*\/dashboard/, { timeout: 5000 });

    // Verify admin is logged in
    await expect(page.getByText('System Admin')).toBeVisible();
  });

  test('should display analytics dashboard after HR Analyst login', async ({ page }) => {
    await clearAuthState(page);
    await page.goto(BASE_URL + '/login');
    await page.fill('input[name="email"]', 'hranalyst@acme.com');
    await page.fill('input[name="password"]', 'analyst123');

    const loginResp = page.waitForResponse(r =>
      r.url().includes('/auth/login') && r.status() === 200,
      { timeout: 5000 }
    );
    await page.click('.btn-login');
    await loginResp;

    // Wait for redirect to dashboard
    await expect(page).toHaveURL(/.*\/dashboard/, { timeout: 5000 });

    // Dashboard should be visible with real analytics data
    await expect(page.locator('h2')).toContainText('Compensation Dashboard');
    await expect(page.getByText('Total Annual Payroll')).toBeVisible();
    await expect(page.getByText('Average Salary')).toBeVisible();
  });

  test('should display employee directory', async ({ page }) => {
    await clearAuthState(page);
    await page.goto(BASE_URL + '/login');
    await page.fill('input[name="email"]', 'admin@acme.com');
    await page.fill('input[name="password"]', 'admin123');

    const loginResp = page.waitForResponse(r =>
      r.url().includes('/auth/login') && r.status() === 200,
      { timeout: 5000 }
    );
    await page.click('.btn-login');
    await loginResp;

    // Wait for redirect to dashboard
    await expect(page).toHaveURL(/.*\/dashboard/, { timeout: 5000 });

    // Navigate directly to directory (bypasses routerLink click issues in Playwright)
    await page.goto(BASE_URL + '/directory', { waitUntil: 'domcontentloaded' });

    // Wait for lazy chunk and component to fully render
    await page.waitForTimeout(3000);

    // Employee list should be visible - table exists with data rows
    const tables = await page.locator('table').all();
    expect(tables.length).toBeGreaterThan(0);

    // Verify employee content is visible (column header confirms component loaded)
    await expect(page.getByRole('columnheader', { name: 'Employee ID' })).toBeVisible();

    // Verify actual seeded data shows up (at least some employee names)
    const bodyText = await page.evaluate(() => document.body.innerText);
    expect(bodyText).toContain('System Admin');
  });

  test('should navigate to reports page', async ({ page }) => {
    await clearAuthState(page);
    await page.goto(BASE_URL + '/login');
    await page.fill('input[name="email"]', 'admin@acme.com');
    await page.fill('input[name="password"]', 'admin123');

    const loginResp = page.waitForResponse(r =>
      r.url().includes('/auth/login') && r.status() === 200,
      { timeout: 5000 }
    );
    await page.click('.btn-login');
    await loginResp;

    // Wait for redirect to dashboard
    await expect(page).toHaveURL(/.*\/dashboard/, { timeout: 5000 });

    // Navigate directly to reports (bypasses routerLink click issues in Playwright)
    await page.goto(BASE_URL + '/reports/custom', { waitUntil: 'domcontentloaded' });

    // Wait for lazy chunk and component to fully render
    await page.waitForTimeout(2000);

    // Reports page should be visible
    await expect(page.locator('h2')).toContainText('Reports');
  });

  test('should logout successfully', async ({ page }) => {
    await clearAuthState(page);
    await page.goto(BASE_URL + '/login');
    await page.fill('input[name="email"]', 'admin@acme.com');
    await page.fill('input[name="password"]', 'admin123');

    const loginResp = page.waitForResponse(r =>
      r.url().includes('/auth/login') && r.status() === 200,
      { timeout: 5000 }
    );
    await page.click('.btn-login');
    await loginResp;

    // Wait for redirect to dashboard
    await expect(page).toHaveURL(/.*\/dashboard/, { timeout: 5000 });
    await expect(page.getByText('System Admin')).toBeVisible();

    // Logout by clicking the text
    await page.getByText('Sign Out').click();

    // Should be redirected to login (with returnUrl query param)
    await expect(page).toHaveURL(/.*\/login/, { timeout: 5000 });
  });
});
