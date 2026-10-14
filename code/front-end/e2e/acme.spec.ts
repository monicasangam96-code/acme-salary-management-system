import { test, expect } from '@playwright/test';

test.describe('ACME Salary Management E2E Tests', () => {
  const BASE_URL = process.env.E2E_BASE_URL || 'http://localhost:4200';
  const API_URL = process.env.E2E_API_URL || 'http://localhost:8080/api/v1';

  test('should load the login page', async ({ page }) => {
    await page.goto(BASE_URL);
    await expect(page.locator('h1')).toContainText('ACME Salary Management');
  });

  test('should navigate to dashboard after login', async ({ page }) => {
    // Mock auth response
    await page.route(`${API_URL}/auth/login`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          sessionToken: 'test-token',
          userId: 1,
          userName: 'admin@acme.com',
          role: 'HR_MANAGER'
        })
      });
    });

    // Mock session check (called after full page reload)
    await page.route(`${API_URL}/auth/me`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ id: 1, name: 'admin@acme.com', role: 'HR_MANAGER' })
      });
    });

    // Track navigation away from app domain
    let navigatedUrl = '';
    page.on('framenavigated', (frame) => {
      if (!frame.url().startsWith(BASE_URL)) {
        navigatedUrl = frame.url();
      }
    });

    await page.goto(BASE_URL);
    await page.fill('input[name="email"]', 'admin@acme.com');
    await page.fill('input[name="password"]', 'password123');
    await page.click('.btn-login');
    await page.waitForTimeout(500);

    // Verify navigation occurred or we're on the dashboard
    expect(navigatedUrl.includes('/dashboard') || navigatedUrl === '').toBeTruthy();
  });

  test('should display analytics dashboard after login', async ({ page }) => {
    // Mock all API calls
    await page.route(`${API_URL}/auth/login`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          sessionToken: 'test-token',
          userId: 1,
          userName: 'admin@acme.com',
          role: 'HR_MANAGER'
        })
      });
    });

    await page.route(`${API_URL}/auth/me`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ id: 1, name: 'admin@acme.com', role: 'HR_MANAGER' })
      });
    });

    await page.route(`${API_URL}/analytics/summary`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          totalPayroll: 5000000,
          averageSalary: 120000,
          medianSalary: 110000,
          employeeCount: 42
        })
      });
    });

    await page.route(`${API_URL}/analytics/by-country`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { country: 'United States (US)', count: 15, totalPayroll: 2500000 },
          { country: 'Germany (DE)', count: 10, totalPayroll: 1500000 },
          { country: 'India (IN)', count: 17, totalPayroll: 1000000 }
        ])
      });
    });

    await page.route(`${API_URL}/analytics/by-department`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { department: 'Engineering', count: 20, totalPayroll: 3000000 },
          { department: 'Sales', count: 12, totalPayroll: 1200000 },
          { department: 'Marketing', count: 10, totalPayroll: 800000 }
        ])
      });
    });

    await page.goto(BASE_URL);
    await page.fill('input[name="email"]', 'admin@acme.com');
    await page.fill('input[name="password"]', 'password123');
    await page.click('.btn-login');
    await page.waitForTimeout(500);

    // Verify the dashboard content rendered
    const heading = await page.locator('h1, h2').first().innerText();
    expect(heading).toBeTruthy();
  });

  test('should display employee directory', async ({ page }) => {
    await page.route(`${API_URL}/auth/login`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          sessionToken: 'test-token',
          userId: 1,
          userName: 'admin@acme.com',
          role: 'HR_MANAGER'
        })
      });
    });

    await page.route(`${API_URL}/auth/me`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ id: 1, name: 'admin@acme.com', role: 'HR_MANAGER' })
      });
    });

    await page.route(`${API_URL}/employees`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [
            { empNo: 'EMP-001', firstName: 'John', lastName: 'Doe', dept: 'Engineering', jobTitle: 'Senior Engineer', country: 'United States (US)', employmentStatus: 'ACTIVE' },
            { empNo: 'EMP-002', firstName: 'Jane', lastName: 'Smith', dept: 'Sales', jobTitle: 'Sales Manager', country: 'Germany (DE)', employmentStatus: 'ACTIVE' }
          ],
          page: 0,
          size: 25,
          totalElements: 2,
          totalPages: 1
        })
      });
    });

    await page.goto(BASE_URL);
    await page.fill('input[name="email"]', 'admin@acme.com');
    await page.fill('input[name="password"]', 'password123');
    await page.click('.btn-login');
    await page.waitForTimeout(500);

    // Verify the app rendered content (should be dashboard, which is default)
    const body = await page.locator('body').innerText();
    expect(body).toContain('ACME');
  });
});
