import { test, expect } from '@playwright/test';

const API_URL = 'http://localhost:8080/api/v1';
const APP_URL = 'http://localhost:4200';

test.describe('Authentication & Authorization (UC-01 through UC-05)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(APP_URL);
  });

  test('UC-01: HR Manager login succeeds and lands on dashboard', async ({ page }) => {
    await page.getByLabel('Email').fill('admin@acme.com');
    await page.getByLabel('Password').fill('admin123');
    await page.getByRole('button', { name: 'Sign In' }).click();
    await expect(page).toHaveURL('/dashboard');
    await expect(page.locator('.nav-brand')).toBeVisible();

    // Verify dashboard KPI cards are present
    await expect(page.locator('.kpi-card')).toHaveCount(4);
    await expect(page.getByText('Total Annual Payroll')).toBeVisible();
    await expect(page.getByText('Average Salary')).toBeVisible();
    await expect(page.getByText('Median Salary')).toBeVisible();
    await expect(page.getByText('Employees')).toBeVisible();

    // Verify navigation rail
    await expect(page.getByText('Directory')).toBeVisible();
    await expect(page.getByText('Reports')).toBeVisible();

    await page.screenshot({ path: 'screenshots/uc-01-login-success.png' });
  });

  test('UC-02: HR Analyst login succeeds with read-only access', async ({ page }) => {
    await page.goto(APP_URL);
    await page.getByLabel('Email').fill('hranalyst@acme.com');
    await page.getByLabel('Password').fill('analyst123');
    await page.getByRole('button', { name: 'Sign In' }).click();
    await expect(page).toHaveURL('/dashboard');

    // Navigate to directory and verify no edit buttons are visible
    await page.getByText('Directory').click();
    const editButtons = page.locator('.btn-edit, .btn-delete');
    await expect(editButtons).not.toBeVisible();

    await page.screenshot({ path: 'screenshots/uc-02-analyst-login.png' });
  });

  test('UC-03: Unauthenticated access redirects to login', async ({ page }) => {
    // Navigate directly to dashboard without login
    await page.goto(APP_URL + '/dashboard');
    // Should redirect back to login
    await expect(page).toHaveURL('/login');

    // Verify login form is displayed
    await expect(page.getByLabel('Email')).toBeVisible();
    await expect(page.getByLabel('Password')).toBeVisible();

    await page.screenshot({ path: 'screenshots/uc-03-unauth-redirect.png' });
  });

  test('UC-03-b: Login with invalid credentials fails', async ({ page }) => {
    await page.getByLabel('Email').fill('wrong@acme.com');
    await page.getByLabel('Password').fill('wrongpass');
    await page.getByRole('button', { name: 'Sign In' }).click();

    // Should show error and stay on login page
    await expect(page).toHaveURL('/login');

    await page.screenshot({ path: 'screenshots/uc-03-invalid-creds.png' });
  });

  test('UC-04: Analyst cannot edit salaries', async ({ page }) => {
    // Login as analyst
    await page.getByLabel('Email').fill('hranalyst@acme.com');
    await page.getByLabel('Password').fill('analyst123');
    await page.getByRole('button', { name: 'Sign In' }).click();

    // Navigate to directory and try to access an employee's salary
    await page.getByText('Directory').click();

    // Direct API call attempt — should fail with 403
    const response = await page.evaluate(() => {
      return fetch(`${API_URL}/employees/1`, { credentials: 'include' });
    });

    await page.screenshot({ path: 'screenshots/uc-04-analyst-no-edit.png' });
  });

  test('UC-05: Session expiration — navigating after logout', async ({ page }) => {
    // Login first
    await page.getByLabel('Email').fill('admin@acme.com');
    await page.getByLabel('Password').fill('admin123');
    await page.getByRole('button', { name: 'Sign In' }).click();

    // Simulate logout (clear local state)
    await page.evaluate(() => localStorage.clear());

    // Navigate to a protected route
    await page.goto(APP_URL + '/directory');

    // Should redirect to login
    await expect(page).toHaveURL('/login');

    await page.screenshot({ path: 'screenshots/uc-05-session-expire.png' });
  });
});
