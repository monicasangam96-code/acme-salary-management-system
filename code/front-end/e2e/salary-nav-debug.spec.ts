import { test, expect } from '@playwright/test';
import * as fs from 'fs';

async function capture(page: any, name: string) {
  const dir = './e2e/screenshots/demo';
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
  await page.screenshot({ path: `${dir}/${name}.png`, fullPage: false });
}

test('click directory row navigates to salary detail', async ({ page }) => {
  // Set up console/error listeners immediately
  const errors: string[] = [];
  page.on('console', msg => {
    if (msg.type() === 'error') {
      errors.push(`JS Error: ${msg.text()} (${msg.location().url}:${msg.location().lineNumber})`);
    }
  });
  page.on('pageerror', err => {
    errors.push(`PAGE ERROR: ${err.message}`);
  });

  // Login
  await page.goto('/login');
  await page.getByLabel('Email').fill('admin@acme.com');
  await page.getByLabel('Password').fill('admin123');
  await page.click('.btn-login');
  await page.waitForResponse(r => r.url().includes('/auth/login') && r.status() === 200);
  await expect(page).toHaveURL(/.*\/dashboard/);

  // Navigate to directory
  await page.goto('http://localhost:4200/directory', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(3000);
  await capture(page, 'directory-before-click');

  // Intercept API calls on salary detail page
  const apiCalls: string[] = [];
  page.on('response', async (resp) => {
    if (resp.url().includes('/api/v1/employees') && resp.ok()) {
      try {
        const body = await resp.text();
        apiCalls.push(`${resp.url()} → ${body.substring(0, 1500)}`);
      } catch {}
    }
  });

  // Click the first employee row (System Admin)
  const row = page.locator('.employee-row').first();
  const empNo = await row.locator('td:first-child').textContent();
  console.log(`=== Clicking row: ${empNo} ===`);
  await row.click();

  // Wait for navigation to salary detail
  await expect(page).toHaveURL(/.*\/salary\/\d+/, { timeout: 5000 });
  const finalUrl = page.url();
  console.log(`=== Final URL: ${finalUrl} ===`);

  // Extract and verify ID
  const idMatch = finalUrl.match(/\/salary\/(\d+)/);
  const empId = idMatch ? idMatch[1] : 'NONE';
  console.log(`=== Employee ID from URL: ${empId} ===`);

  // Wait for rendering
  await page.waitForTimeout(4000);

  // Print API responses
  console.log('=== API Responses ===');
  for (const call of apiCalls) {
    console.log(call);
  }

  // Check rendered content
  const h2Text = await page.locator('.salary-detail h2').first().textContent().catch(() => 'NO_HEADER');
  console.log(`Salary Detail H2: "${h2Text}"`);

  const pTexts = await page.locator('.salary-detail p').allTextContents();
  console.log(`p texts: ${JSON.stringify(pTexts)}`);

  // Check loading state
  if (await page.getByText('Loading employee...').isVisible()) {
    console.log('PROBLEM: Still showing "Loading employee..."');
  } else {
    console.log('OK: Employee data loaded and displayed');
  }

  // Print errors
  for (const err of errors) {
    console.log(`ERROR: ${err}`);
  }

  await capture(page, 'salary-detail-after-click');
});
