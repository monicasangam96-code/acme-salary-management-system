import { test, expect } from '@playwright/test';

test('capture salary detail errors', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', err => {
    errors.push(`${err.message} at ${err.filename || 'unknown'}:${err.lineNumber}`);
  });

  await page.goto('/login');
  await page.getByLabel('Email').fill('admin@acme.com');
  await page.getByLabel('Password').fill('admin123');
  await page.getByRole('button', { name: 'Sign In' }).click();
  await expect(page).toHaveURL(/\/dashboard/);

  // Navigate directly to salary detail
  await page.goto('http://localhost:4200/salary/1');
  await page.waitForTimeout(3000);

  // Print ALL errors
  for (const e of errors) {
    console.log(`FULL_ERROR: ${e}`);
  }

  // Also capture the page
  if (errors.length > 0) {
    await page.screenshot({ path: './e2e/screenshots/demo/salary-detail-error.png', fullPage: true });
  }
});
