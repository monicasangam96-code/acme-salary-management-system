/**
 * Capture screenshots of all app pages for demo materials.
 * Run with: npx playwright test e2e/capture-demo.spec.ts
 */
import { test, expect } from '@playwright/test';
import * as fs from 'fs';

const SCREENSHOT_DIR = './e2e/screenshots/demo';

async function ensureDir() {
  if (!fs.existsSync(SCREENSHOT_DIR)) fs.mkdirSync(SCREENSHOT_DIR, { recursive: true });
}

async function login(page: any) {
  await page.goto('/login');
  await expect(page).toHaveURL('/login');
  await page.getByLabel('Email').fill('admin@acme.com');
  await page.getByLabel('Password').fill('admin123');
  await page.getByRole('button', { name: 'Sign In' }).click();
  await expect(page).toHaveURL(/\/dashboard/);
}

async function capture(page: any, name: string) {
  await ensureDir();
  await page.screenshot({ path: `${SCREENSHOT_DIR}/${name}.png`, fullPage: false });
}

// ===================== LOGIN SCREENSHOT =====================

test('Login Page', async ({ page }) => {
  await page.goto('/login');
  await capture(page, '01-login-page');
});

// ===================== DASHBOARD SCREENSHOTS =====================

test('Dashboard - Overview (All Data)', async ({ page }) => {
  await login(page);
  await capture(page, '02-dashboard-overview');
});

// ===================== DIRECTORY SCREENSHOTS =====================

test('Directory - All Active Employees', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Directory' }).click();
  await expect(page).toHaveURL(/\/directory/);
  await capture(page, '03-directory-all-active');
});

test('Directory - Filter by Engineering', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Directory' }).click();
  // select[1] = department filter
  await page.locator('.filter-select').nth(1).evaluate((el: HTMLSelectElement) => { el.value = 'Engineering'; el.dispatchEvent(new Event('change')); });
  await capture(page, '04-directory-filter-eng');
});

test('Directory - Filter by United States', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Directory' }).click();
  // select[0] = country filter
  await page.locator('.filter-select').nth(0).evaluate((el: HTMLSelectElement) => { el.value = 'United States (US)'; el.dispatchEvent(new Event('change')); });
  await capture(page, '05-directory-filter-us');
});

test('Directory - Filter by HR Department', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Directory' }).click();
  await page.locator('.filter-select').nth(1).evaluate((el: HTMLSelectElement) => { el.value = 'HR'; el.dispatchEvent(new Event('change')); });
  await capture(page, '06-directory-filter-hr');
});

test('Directory - Filter by Japan', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Directory' }).click();
  await page.locator('.filter-select').nth(0).evaluate((el: HTMLSelectElement) => { el.value = 'Japan (JP)'; el.dispatchEvent(new Event('change')); });
  await capture(page, '07-directory-filter-japan');
});

test('Directory - Second Page of Results', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Directory' }).click();
  const nextBtn = page.locator('.page-btn').nth(1);
  if (await nextBtn.isEnabled()) {
    await nextBtn.click();
  }
  await capture(page, '08-directory-page-2');
});

// ===================== REPORTS SCREENSHOTS =====================

test('Reports - Average Salary by Country', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Reports' }).click();
  await expect(page).toHaveURL(/\/reports/);
  await page.locator('.template-select').evaluate((el: HTMLSelectElement) => { el.value = 'avg-by-country'; el.dispatchEvent(new Event('change')); });
  await page.waitForSelector('.data-table tbody tr', { timeout: 10000 });
  await capture(page, '09-report-country-salary');
});

test('Reports - Highest Departments Payroll', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Reports' }).click();
  await page.locator('.template-select').evaluate((el: HTMLSelectElement) => { el.value = 'highest-departments'; el.dispatchEvent(new Event('change')); });
  await page.waitForSelector('.data-table tbody tr', { timeout: 10000 });
  await capture(page, '10-report-dept-payroll');
});

test('Reports - Job Level Distribution', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Reports' }).click();
  await page.locator('.template-select').evaluate((el: HTMLSelectElement) => { el.value = 'level-distribution'; el.dispatchEvent(new Event('change')); });
  await page.waitForSelector('.data-table tbody tr', { timeout: 10000 });
  await capture(page, '11-report-job-level');
});

test('Reports - Payroll by Department', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Reports' }).click();
  // Use highest-departments template (shows payroll by department)
  await page.locator('.template-select').evaluate((el: HTMLSelectElement) => { el.value = 'highest-departments'; el.dispatchEvent(new Event('change')); });
  await page.waitForSelector('.data-table tbody tr', { timeout: 10000 });
  // Give chart time to render
  await page.waitForTimeout(1500);
  await capture(page, '12-report-payroll-dept');
});

// ===================== SALARY DETAILS SCREENSHOT =====================

test('Salary - Detail View', async ({ page }) => {
  await login(page);
  await page.goto('/salary/2');
  await capture(page, '13-salary-detail');
});

test('Salary - Salary History Tab', async ({ page }) => {
  await login(page);
  await page.goto('/salary/2');
  await page.waitForSelector('.tabs button', { timeout: 5000 });
  await page.locator('.tabs button').first().click();
  await page.waitForTimeout(1000);
  await capture(page, '14-salary-history-tab');
});

// ===================== ANALYTICS SCREENSHOT =====================

test('Analytics Dashboard (HR Analyst)', async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel('Email').fill('analyst@acme.com');
  await page.getByLabel('Password').fill('analyst123');
  await page.getByRole('button', { name: 'Sign In' }).click();
  // Wait for any post-login content (dashboard or analytics view)
  await expect(page.locator('.dashboard-overview, .analytics-view, h1, h2')).toBeVisible({ timeout: 5000 });
  await capture(page, '15-analytics-dashboard');
});

// ===================== ADDITIONAL DIRECTORY FILTERS =====================

test('Directory - Filter by Engineering (United States)', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Directory' }).click();
  await page.locator('.filter-select').nth(0).evaluate((el: HTMLSelectElement) => { el.value = 'United States (US)'; el.dispatchEvent(new Event('change')); });
  await page.waitForTimeout(500);
  await page.locator('.filter-select').nth(1).evaluate((el: HTMLSelectElement) => { el.value = 'Engineering'; el.dispatchEvent(new Event('change')); });
  await capture(page, '16-directory-filter-us-eng');
});

test('Directory - Filter by Japan (IT)', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Directory' }).click();
  await page.locator('.filter-select').nth(0).evaluate((el: HTMLSelectElement) => { el.value = 'Japan (JP)'; el.dispatchEvent(new Event('change')); });
  await page.waitForTimeout(500);
  await page.locator('.filter-select').nth(1).evaluate((el: HTMLSelectElement) => { el.value = 'IT'; el.dispatchEvent(new Event('change')); });
  await capture(page, '17-directory-filter-japan-it');
});
