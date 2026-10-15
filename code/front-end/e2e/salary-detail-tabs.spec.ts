import { test, expect } from '@playwright/test';
import * as fs from 'fs';

const SCREENSHOT_DIR = './e2e/screenshots/demo';

test.beforeEach(async ({ page }) => {
  if (!fs.existsSync(SCREENSHOT_DIR)) fs.mkdirSync(SCREENSHOT_DIR, { recursive: true });
  await page.goto('/login');
  await page.getByLabel('Email').fill('admin@acme.com');
  await page.getByLabel('Password').fill('admin123');
  await page.getByRole('button', { name: 'Sign In' }).click();
  await expect(page).toHaveURL(/\/dashboard/);
});

test('Salary Detail - Salary History tab with data', async ({ page }) => {
  await page.goto('http://localhost:4200/salary/1');
  await page.waitForTimeout(3000);

  // Verify employee name and status are displayed
  await expect(page.locator('.salary-detail h2')).toContainText('System Admin', { timeout: 5000 });
  const pLocator = page.locator('.salary-detail p').last();
  await expect(pLocator).toContainText('ACTIVE');

  // Click Salary History tab
  await page.locator('.tabs button').first().click();
  await page.waitForTimeout(1000);
  await page.screenshot({ path: `${SCREENSHOT_DIR}/14-salary-history-tab.png`, fullPage: false });

  // Verify salary history data is displayed (should have at least one row)
  const rows = page.locator('app-salary-history .data-table tbody tr');
  await expect(rows.first()).toBeVisible({ timeout: 5000 });
});

test('Salary Detail - Payroll tab', async ({ page }) => {
  await page.goto('http://localhost:4200/salary/1');
  await page.waitForTimeout(3000);

  // Click Payroll tab
  await page.locator('.tabs button').nth(1).click();
  await page.waitForTimeout(1000);
  await page.screenshot({ path: `${SCREENSHOT_DIR}/15-payroll-tab.png`, fullPage: false });
});
