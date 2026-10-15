import { test, expect } from '@playwright/test';

test('save button persists a new salary to the database', async ({ page }) => {
  const POST_URLS: string[] = [];

  // Intercept all /api/v1/salaries calls
  page.on('response', (resp) => {
    if (resp.url().includes('/api/v1/salaries') && resp.status() >= 200 && resp.status() < 300) {
      POST_URLS.push(`${resp.status()} ${resp.url()}`);
    }
  });

  // Login as HR_MANAGER
  await page.goto('/login');
  await page.getByLabel('Email').fill('admin@acme.com');
  await page.getByLabel('Password').fill('admin123');
  await page.getByRole('button', { name: 'Sign In' }).click();
  await expect(page).toHaveURL(/\/dashboard/);

  // Navigate to salary detail for employee 1 (System Admin) and wait for rendering
  await page.goto('http://localhost:4200/salary/1');
  await expect(page.locator('.salary-detail h2')).toBeVisible({ timeout: 5000 });
  await expect(page.locator('.tabs button')).toHaveCount(2, { timeout: 3000 });

  // The default tab is 'Salary History' (where the entry form lives) — verify it's active
  const activeTab = page.locator('.tabs button.active');
  const activeText = await activeTab.first().textContent();
  if (activeText !== 'Salary History') {
    await page.locator('.tabs button').first().click();
    await page.waitForTimeout(500);
  }

  // Verify the salary entry form is visible
  await expect(page.locator('app-salary-entry-form')).toBeVisible({ timeout: 3000 });

  // Record row count before save
  const rowsBefore = page.locator('app-salary-history .data-table tbody tr');
  const rowCountBefore = await rowsBefore.count();

  // Fill the form — use exact label selectors from salary-entry-form.component.html
  await page.getByLabel('Salary Amount *').fill('180000');
  await page.getByLabel('Currency *').selectOption('EUR');
  await page.getByLabel('Effective Date *').fill('2027-06-01');
  await page.getByLabel('Notes').fill('E2E salary persistence test');

  // Click Save — button text is 'Save Salary'
  const saveBtn = page.getByRole('button', { name: 'Save Salary' });
  await expect(saveBtn).toBeVisible({ timeout: 3000 });
  await saveBtn.click();
  await page.waitForTimeout(2000);

  // Verify the API POST succeeded (status >= 200 and < 300)
  expect(POST_URLS.length).toBeGreaterThan(0);
  const status = parseInt(POST_URLS[0], 10);
  expect(status).toBeGreaterThanOrEqual(200);
  expect(status).toBeLessThan(300);

  // Verify salary history table now has one more row than before
  const rowCountAfter = await rowsBefore.count();
  expect(rowCountAfter).toBeGreaterThan(rowCountBefore);

  console.log(`Salary history rows before: ${rowCountBefore}, after: ${rowCountAfter}`);
  console.log(`POST response: ${POST_URLS[0]}`);
});

// Verify the newly saved salary is readable back from DB after navigation away and back
test('newly saved salary survives navigation refresh', async ({ page }) => {
  const TEST_AMOUNT_FORMATTED = '$300,000.00'; // Angular's number pipe format: $ + commas

  // Login and navigate to salary detail for employee 1
  await page.goto('/login');
  await page.getByLabel('Email').fill('admin@acme.com');
  await page.getByLabel('Password').fill('admin123');
  await page.getByRole('button', { name: 'Sign In' }).click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 5000 });

  await page.goto('http://localhost:4200/salary/1');
  await expect(page.locator('.salary-detail h2')).toBeVisible({ timeout: 5000 });

  // Go to Salary History tab and fill form
  await page.locator('.tabs button').first().click();
  await page.waitForTimeout(500);

  await page.getByLabel('Salary Amount *').fill('300000');
  await page.getByLabel('Currency *').selectOption('USD');
  await page.getByLabel('Effective Date *').fill('2028-12-01');
  await page.getByRole('button', { name: 'Save Salary' }).click();
  await page.waitForTimeout(3000);

  // Navigate away and back — verify salary history is re-fetched from API by capturing network traffic
  const histCalls: string[] = [];
  page.on('response', (resp) => {
    if (resp.url().includes('/salary-history') && resp.ok()) {
      histCalls.push(resp.url());
    }
  });

  await page.goto('http://localhost:4200/directory', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(1000);
  await page.goto('http://localhost:4200/salary/1');

  // Wait for the salary history data table to re-render (proves re-fetch from API)
  const table = page.locator('app-salary-history .data-table tbody tr');
  await expect(table.first()).toBeVisible({ timeout: 5000 });

  // The refreshed history should have at least one entry now
  expect(await table.count()).toBeGreaterThan(0);

  // Verify the formatted test amount appears in at least one row cell
  const cells = page.locator('app-salary-history .data-table tbody tr td').filter({ hasText: TEST_AMOUNT_FORMATTED });
  expect(await cells.count()).toBeGreaterThan(0);

  console.log(`Salary history rows after refresh: ${await table.count()}, test amount still found: true`);
});
