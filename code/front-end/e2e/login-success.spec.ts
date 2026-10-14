import { test, expect } from '@playwright/test';

test('admin login redirects to dashboard', async ({ page }) => {
  await page.goto('http://localhost:4200/login');
  await page.waitForLoadState('networkidle');

  // Fill and submit credentials
  await page.fill('input[name="email"]', 'admin@acme.com');
  await page.fill('input[name="password"]', 'admin123');

  const loginResp = page.waitForResponse(r =>
    r.url().includes('/auth/login') && r.status() === 200,
    { timeout: 5000 }
  );

  await Promise.all([page.click('.btn-login'), loginResp]);

  // Wait for Angular to navigate and render
  await expect(page).toHaveURL(/.*\/dashboard/, { timeout: 10000 });

  // Verify dashboard header content
  await expect(page.getByText('System Admin')).toBeVisible();
  await expect(page.getByRole('link', { name: 'Dashboard' })).toBeVisible();
});

test('analyst login redirects to dashboard', async ({ page }) => {
  await page.goto('http://localhost:4200/login');
  await page.waitForLoadState('networkidle');

  // Fill and submit credentials
  await page.fill('input[name="email"]', 'hranalyst@acme.com');
  await page.fill('input[name="password"]', 'analyst123');

  const loginResp = page.waitForResponse(r =>
    r.url().includes('/auth/login') && r.status() === 200,
    { timeout: 5000 }
  );

  await Promise.all([page.click('.btn-login'), loginResp]);

  // Wait for Angular to navigate and render
  await expect(page).toHaveURL(/.*\/dashboard/, { timeout: 10000 });

  // Verify dashboard header content (analyst-specific)
  await expect(page.getByText('HR Analyst')).toBeVisible();
  await expect(page.getByRole('link', { name: 'Dashboard' })).toBeVisible();
});

test('invalid credentials show error', async ({ page }) => {
  let loginResponseStatus = 0;

  page.on('response', r => {
    if (r.url().includes('/auth/login')) loginResponseStatus = r.status();
  });

  await page.goto('http://localhost:4200/login');
  await page.fill('input[name="email"]', 'admin@acme.com');
  await page.fill('input[name="password"]', 'wrongpassword');

  await page.click('.btn-login');
  await page.waitForTimeout(2000);

  expect(loginResponseStatus).toBe(401);
  await expect(page).toHaveURL(/.*\/login/);
});
