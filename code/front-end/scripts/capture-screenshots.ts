/**
 * Capture screenshots of all app pages for demo materials.
 * Run with: npx playwright test --config scripts/playwright-screenshot.config.ts
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

test('Dashboard - Overview', async ({ page }) => {
  await login(page);
  await capture(page, '02-dashboard-overview');
});

test('Dashboard - Filter by United States', async ({ page }) => {
  await login(page);
  // Select country filter (first select)
  await page.locator('select').first().fill('United States (US)');
  await capture(page, '03-dashboard-us-filter');
});

test('Dashboard - Filter by Engineering', async ({ page }) => {
  await login(page);
  // Select department filter (second select)
  await page.locator('select').nth(1).fill('Engineering');
  await capture(page, '04-dashboard-eng-filter');
});

// ===================== DIRECTORY SCREENSHOTS =====================

test('Directory - All Active Employees', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Directory' }).click();
  await expect(page).toHaveURL(/\/directory/);
  await capture(page, '05-directory-all');
});

test('Directory - Filter by Engineering Dept', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Directory' }).click();
  // Country filter is first select, department is second
  await page.locator('select').nth(1).fill('Engineering');
  await capture(page, '06-directory-eng-filter');
});

test('Directory - Filter by United Kingdom', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Directory' }).click();
  await page.locator('select').first().fill('United Kingdom (UK)');
  await capture(page, '07-directory-uk-filter');
});

test('Directory - Filter by HR Dept', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Directory' }).click();
  await page.locator('select').nth(1).fill('HR');
  await capture(page, '08-directory-hr-filter');
});

test('Directory - Filter by Japan', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Directory' }).click();
  await page.locator('select').first().fill('Japan (JP)');
  await capture(page, '09-directory-japan-filter');
});

// ===================== REPORTS SCREENSHOTS =====================

test('Reports - Average Salary by Country', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Reports' }).click();
  await expect(page).toHaveURL(/\/reports/);
  // Use the dropdown to select template
  await page.locator('select').first().fill('avg-by-country');
  await capture(page, '10-report-country-salary');
});

test('Reports - Highest Departments Payroll', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Reports' }).click();
  await page.locator('select').first().fill('highest-departments');
  await capture(page, '11-report-dept-payroll');
});

test('Reports - Job Level Distribution', async ({ page }) => {
  await login(page);
  await page.getByRole('link', { name: 'Reports' }).click();
  await page.locator('select').first().fill('level-distribution');
  await capture(page, '12-report-job-level');
});

// ===================== SALARY DETAILS SCREENSHOT =====================

test('Salary - Detail View', async ({ page }) => {
  await login(page);
  await page.goto('/salary/2');
  await capture(page, '13-salary-detail');
});
