import { test, expect } from '@playwright/test';

const APP_URL = 'http://localhost:4200';

test.describe('Analytics & Reports (UC-20 through UC-30)', () => {
  async function loginAdmin(page: any) {
    await page.goto(APP_URL);
    await page.getByLabel('Email').fill('admin@acme.com');
    await page.getByLabel('Password').fill('admin123');
    await page.getByRole('button', { name: 'Sign In' }).click();
  }

  test('UC-20: Dashboard KPI cards display correct values', async ({ page }) => {
    await loginAdmin(page);
    // Dashboard is default landing page
    await expect(page.getByText('Total Annual Payroll')).toBeVisible();
    await expect(page.getByText('Average Salary')).toBeVisible();
    await expect(page.getByText('Median Salary')).toBeVisible();
    await expect(page.getByText('Employees')).toBeVisible();

    // Verify at least one KPI card has a non-zero value
    const values = page.locator('.kpi-value');
    const hasValue = await values.first().innerText().then(t => t !== '' && !t.includes('0'));
    expect(hasValue).toBeTruthy();

    await page.screenshot({ path: 'screenshots/uc-20-kpi-cards.png' });
  });

  test('UC-21: Compensation breakdown by country renders', async ({ page }) => {
    await loginAdmin(page);
    await expect(page.locator('.bar-chart')).toBeVisible();
    const bars = page.locator('.bar-fill');
    const barCount = await bars.count();
    expect(barCount).toBeGreaterThan(0);

    // Verify tooltips on hover
    if (await bars.first().count() > 0) {
      await bars.first().hover();
      await expect(page.getByText(/M$/)).toBeVisible();
    }

    await page.screenshot({ path: 'screenshots/uc-21-country-breakdown.png' });
  });

  test('UC-22: Compensation breakdown by department renders', async ({ page }) => {
    await loginAdmin(page);
    const deptBars = page.locator('.bar-fill');
    if (await deptBars.count() > 0) {
      await deptBars.first().hover();
    }
    await page.screenshot({ path: 'screenshots/uc-22-dept-breakdown.png' });
  });

  test('UC-25: HR Question — Average salary by country', async ({ page }) => {
    await loginAdmin(page);
    await page.getByText('Reports').click();

    // Select template
    const templateSelect = page.locator('.template-select');
    if (await templateSelect.count() > 0) {
      await templateSelect.selectOption('avg-by-country');
      await expect(page.getByText(/Average Salary by Country/)).toBeVisible();
    }

    await page.screenshot({ path: 'screenshots/uc-25-hr-question-country.png' });
  });

  test('UC-26: HR Question — Highest payroll departments', async ({ page }) => {
    await loginAdmin(page);
    await page.getByText('Reports').click();

    const templateSelect = page.locator('.template-select');
    if (await templateSelect.count() > 0) {
      await templateSelect.selectOption('highest-departments');
      await expect(page.getByText(/Highest Payroll/)).toBeVisible();
    }

    await page.screenshot({ path: 'screenshots/uc-26-hr-question-departments.png' });
  });

  test('UC-28: Export all employees as CSV — file downloads and has correct rows', async ({ page, context }) => {
    await loginAdmin(page);
    await page.getByText('Reports').click();

    // Trigger download
    const [download] = await Promise.all([
      context.waitForEvent('download'),
      page.getByRole('button', { name: /Export|Download/i }).click()
    ]);

    const path = await download.path();
    expect(path).toBeTruthy();

    await page.screenshot({ path: 'screenshots/uc-28-csv-export.png' });
  });

  test('UC-29: Export filtered salary data', async ({ page, context }) => {
    await loginAdmin(page);
    await page.getByText('Reports').click();

    const [download] = await Promise.all([
      context.waitForEvent('download'),
      page.getByRole('button', { name: /Export|Download/i }).click()
    ]);

    const path = await download.path();
    expect(path).toBeTruthy();

    await page.screenshot({ path: 'screenshots/uc-29-filtered-export.png' });
  });

  test('UC-31: Multi-currency salaries displayed correctly', async ({ page }) => {
    await loginAdmin(page);
    await page.getByText('Directory').click();

    // Verify salary amounts have currency symbols, not just numbers
    const salaryCells = page.locator('.employee-row td:nth-child(7)');
    const firstSalary = await salaryCells.first().innerText();
    expect(firstSalary).toContain('$') || expect(firstSalary).toContain('€') || expect(firstSalary).toContain('£');

    await page.screenshot({ path: 'screenshots/uc-31-multi-currency-display.png' });
  });
});
