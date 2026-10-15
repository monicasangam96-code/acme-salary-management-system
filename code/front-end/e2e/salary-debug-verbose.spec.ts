import { test, expect } from '@playwright/test';

test('verbose salary detail error debug', async ({ page }) => {
  const errors: string[] = [];
  const logs: string[] = [];

  // Catch ALL console output
  page.on('console', msg => {
    const text = msg.text();
    logs.push(`[${msg.type()}] ${text}`);
  });
  page.on('pageerror', err => {
    errors.push(`${err.message} at ${err.filename || 'unknown'}:${err.lineNumber}`);
  });

  // Login
  await page.goto('/login');
  await page.getByLabel('Email').fill('admin@acme.com');
  await page.getByLabel('Password').fill('admin123');
  await page.getByRole('button', { name: 'Sign In' }).click();
  await expect(page).toHaveURL(/\/dashboard/);

  // Navigate to salary detail (which lazy-loads the component)
  await page.goto('http://localhost:4200/salary/1');
  await page.waitForTimeout(3000);

  console.log('\n=== PAGE ERRORS ===');
  errors.forEach(e => console.log(e));

  console.log('\n=== CONSOLE LOGS ===');
  logs.filter(l => !l.includes('[log]') && !l.includes('[info]')).forEach(l => console.log(l));

  // Check if salary detail is actually displayed
  const h2 = await page.locator('.salary-detail h2').first().textContent();
  console.log(`\nSalary Detail H2: "${h2}"`);

  const pLocator = page.locator('.salary-detail p');
  const pCount = await pLocator.count();
  for (let i = 0; i < pCount; i++) {
    console.log(`p[${i}]: "${await pLocator.nth(i).textContent()}"`);
  }

  // Check if salary history data table exists
  const salaryTable = page.locator('app-salary-history .data-table');
  const salaryVisible = await salaryTable.isVisible().catch(() => false);
  console.log(`\nSalary History Table visible: ${salaryVisible}`);

  const salaryRows = page.locator('app-salary-history .data-table tbody tr');
  const rowCount = await salaryRows.count();
  console.log(`Salary History Rows: ${rowCount}`);

  // Check if salary entry form exists
  const formVisible = await page.locator('app-salary-entry-form').isVisible().catch(() => false);
  console.log(`\nSalary Entry Form visible: ${formVisible}`);
});
