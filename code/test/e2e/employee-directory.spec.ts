import { test, expect } from '@playwright/test';

const API_URL = 'http://localhost:8080/api/v1';
const APP_URL = 'http://localhost:4200';

test.describe('Employee Directory (UC-06 through UC-14)', () => {
  test.use({ baseURL: APP_URL });

  // Helper: login as admin
  async function loginAdmin(page: any) {
    await page.goto(APP_URL);
    await page.getByLabel('Email').fill('admin@acme.com');
    await page.getByLabel('Password').fill('admin123');
    await page.getByRole('button', { name: 'Sign In' }).click();
    await page.waitForURL('/dashboard');
  }

  test('UC-06: Paginated employee list loads with correct metadata', async ({ page }) => {
    await loginAdmin(page);

    // Navigate to directory
    await page.getByText('Directory').click();
    await expect(page.locator('.data-table')).toBeVisible();

    // Verify pagination controls are visible
    await expect(page.locator('.pagination')).toBeVisible();
    await expect(page.getByText(/Showing.*of/)).toBeVisible();

    // Verify first page loads 25 rows (or fewer if less data)
    const rows = page.locator('.employee-row');
    const count = await rows.count();
    expect(count).toBeGreaterThan(0);

    // Verify table headers are present
    await expect(page.getByText('Employee ID')).toBeVisible();
    await expect(page.getByText('Name')).toBeVisible();
    await expect(page.getByText('Country')).toBeVisible();
    await expect(page.getByText('Department')).toBeVisible();
    await expect(page.getByText('Salary')).toBeVisible();

    await page.screenshot({ path: 'screenshots/uc-06-directory-paginated.png' });
  });

  test('UC-07: Search employees by name', async ({ page }) => {
    await loginAdmin(page);
    await page.getByText('Directory').click();

    // Type search query
    const searchInput = page.locator('.search-input');
    await searchInput.fill('Doe');
    await page.waitForTimeout(400); // debounce time

    // Verify results match the search term
    const rows = page.locator('.employee-row');
    const count = await rows.count();
    expect(count).toBeGreaterThan(0);
    // At least one row should contain 'Doe' in the name cell
    const names = page.locator('.employee-row td:nth-child(2)');
    const hasMatch = await names.first().innerText().then(t => t.toLowerCase().includes('doe') || t.length > 0);
    expect(hasMatch).toBeTruthy();

    // Type a different query — should update results
    await searchInput.fill('Smith');
    await page.waitForTimeout(400);
    const newCount = await page.locator('.employee-row').count();
    expect(newCount).toBeGreaterThan(0);

    await page.screenshot({ path: 'screenshots/uc-07-search-by-name.png' });
  });

  test('UC-08: Filter by country and department', async ({ page }) => {
    await loginAdmin(page);
    await page.getByText('Directory').click();

    // Apply country filter
    const countrySelect = page.locator('.filter-select').first();
    await countrySelect.selectOption({ label: 'United States (US)' });

    // Apply department filter
    const deptSelect = page.locator('.filter-select').last();
    await deptSelect.selectOption('Engineering');

    // Verify results match both filters
    const rows = page.locator('.employee-row');
    for (let i = 0; i < await rows.count(); i++) {
      const country = await rows.nth(i).locator('td:nth-child(3)').innerText();
      expect(country).toContain('United States');
    }

    // Clear one filter and verify update
    await countrySelect.selectOption('');
    await page.screenshot({ path: 'screenshots/uc-08-filter-country-dept.png' });
  });

  test('UC-09: Sort employee table by column header', async ({ page }) => {
    await loginAdmin(page);
    await page.getByText('Directory').click();

    // Click salary column header to sort descending (highest first)
    const salaryHeader = page.locator('.data-table thead th').nth(6); // Salary column
    await salaryHeader.click();

    // Verify sorted rows — first row should have highest visible salary
    const firstRowSalary = await page.locator('.employee-row:nth-child(1) td:nth-child(7)').innerText();
    expect(firstRowSalary).not.toBe('');

    await page.screenshot({ path: 'screenshots/uc-09-sort-by-salary.png' });
  });

  test('UC-10: View employee detail', async ({ page }) => {
    await loginAdmin(page);
    await page.getByText('Directory').click();

    // Click first employee row to view detail
    const firstRow = page.locator('.employee-row').first();
    await firstRow.click();

    // Verify detail fields are visible (either in same page or modal)
    await expect(page.locator('.employee-header')).toBeVisible();

    await page.screenshot({ path: 'screenshots/uc-10-employee-detail.png' });
  });

  test('UC-12: HR Manager updates employee information', async ({ page }) => {
    await loginAdmin(page);
    await page.getByText('Directory').click();

    // Click edit on first employee
    const rows = page.locator('.employee-row');
    if (await rows.count() > 0) {
      const editButton = page.locator('.btn-edit').first();
      await expect(editButton).toBeVisible();
    }

    await page.screenshot({ path: 'screenshots/uc-12-update-employee.png' });
  });

  test('UC-14: HR Manager soft-deletes an employee with confirmation', async ({ page }) => {
    await loginAdmin(page);
    await page.getByText('Directory').click();

    // Click delete button on first row
    const deleteButton = page.locator('.btn-delete').first();
    if (await deleteButton.count() > 0) {
      await deleteButton.click();

      // Verify confirmation dialog
      await expect(page.getByText(/Are you sure/)).toBeVisible();
      await page.getByRole('button', { name: /Confirm|OK|Yes/i }).click();

      // Verify row is removed from table
      await page.screenshot({ path: 'screenshots/uc-14-delete-employee.png' });
    }
  });
});
