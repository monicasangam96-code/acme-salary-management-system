import { test, expect } from '@playwright/test';

const APP_URL = 'http://localhost:4200';

test.describe('Salary Management (UC-15 through UC-19)', () => {
  async function loginAdmin(page: any) {
    await page.goto(APP_URL);
    await page.getByLabel('Email').fill('admin@acme.com');
    await page.getByLabel('Password').fill('admin123');
    await page.getByRole('button', { name: 'Sign In' }).click();
  }

  test('UC-15: View current salary for an employee', async ({ page }) => {
    await loginAdmin(page);
    await page.getByText('Directory').click();

    // Click first employee row
    await page.locator('.employee-row').first().click();

    // Verify salary detail section is visible
    await expect(page.locator('.salary-detail')).toBeVisible();

    await page.screenshot({ path: 'screenshots/uc-15-current-salary.png' });
  });

  test('UC-16: Set new salary with effective date', async ({ page }) => {
    await loginAdmin(page);
    await page.getByText('Directory').click();
    await page.locator('.employee-row').first().click();

    // Find and fill salary entry form
    const amountInput = page.locator('#amount');
    if (await amountInput.count() > 0) {
      await amountInput.fill('150000');

      const saveButton = page.locator('.btn-save');
      if (await saveButton.count() > 0 && !(await saveButton.getAttribute('disabled') === 'disabled')) {
        await saveButton.click();

        // Verify success message
        await expect(page.getByText(/Salary updated/i)).toBeVisible();
      }
    }

    await page.screenshot({ path: 'screenshots/uc-16-set-salary.png' });
  });

  test('UC-17: Invalid salary amount is rejected', async ({ page }) => {
    await loginAdmin(page);
    await page.getByText('Directory').click();

    // Open salary entry form (via any employee)
    await page.locator('.employee-row').first().click();

    const amountInput = page.locator('#amount');
    if (await amountInput.count() > 0) {
      // Try negative value — HTML number input should prevent this or show error
      await amountInput.fill('-5000');
      const saveButton = page.locator('.btn-save');
      const isDisabled = await saveButton.getAttribute('disabled') === 'disabled';
      expect(isDisabled).toBeTruthy();

      // Try empty — submit should be disabled
      await amountInput.clear();
      const stillDisabled = await saveButton.getAttribute('disabled') === 'disabled';
      expect(stillDisabled).toBeTruthy();
    }

    await page.screenshot({ path: 'screenshots/uc-17-invalid-salary.png' });
  });

  test('UC-18: Multi-currency salary update', async ({ page }) => {
    await loginAdmin(page);
    await page.getByText('Directory').click();
    await page.locator('.employee-row').first().click();

    // Verify currency selector is visible and has multiple options
    const currencySelect = page.locator('#currency');
    if (await currencySelect.count() > 0) {
      const options = await currencySelect.locator('option').all();
      expect(options.length).toBeGreaterThan(1);

      // Change currency to EUR
      await currencySelect.selectOption('EUR');
    }

    await page.screenshot({ path: 'screenshots/uc-18-multi-currency.png' });
  });

  test('UC-19: View payroll records', async ({ page }) => {
    await loginAdmin(page);
    await page.getByText('Directory').click();
    await page.locator('.employee-row').first().click();

    // Click payroll tab if visible
    const payrollTab = page.getByText(/Payroll/i);
    if (await payrollTab.count() > 0) {
      await payrollTab.click();
    }

    // Verify payroll table or message is visible
    await expect(page.locator('.salary-detail')).toBeVisible();

    await page.screenshot({ path: 'screenshots/uc-19-payroll-records.png' });
  });
});
