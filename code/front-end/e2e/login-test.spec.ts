import { test, expect } from '@playwright/test';

test('login works end-to-end', async ({ page }) => {
  // Capture all /api calls for debugging
  const apiLog: string[] = [];

  page.on('response', async (r) => {
    if (r.url().includes('/api/v1/auth/')) {
      let body = '';
      try { body = await r.text(); } catch {}
      const cookieH = r.headers()['cookie'] || '(none)';
      apiLog.push(`${r.status} ${r.url()} | Cookie: ${cookieH.substring(0, 60)} | Body: ${body.substring(0, 80)}`);
    }
  });

  // Go directly to /login
  await page.goto('http://localhost:4200/login');
  console.log('\n[1] At /login:', page.url());

  const h1 = await page.locator('h1').first().textContent();
  console.log(`   H1: ${h1}`);

  // Fill credentials
  await page.fill('input[name="email"]', 'admin@acme.com');
  await page.fill('input[name="password"]', 'admin123');
  console.log('[2] Filled credentials');

  const respPromise = page.waitForResponse(r => r.url().includes('/auth/login'), { timeout: 5000 });

  await Promise.all([
    page.click('.btn-login'),
    respPromise,
  ]);

  // Check cookies after login
  const cookies = await page.context().cookies('http://localhost:4200');
  console.log(`[3] Cookies after login: ${JSON.stringify(cookies.map(c => `${c.name}=${c.value.substring(0,16)}...`))}`);

  // Wait for any app redirect
  await page.waitForTimeout(2000);

  const finalUrl = page.url();
  console.log(`[4] Final URL: ${finalUrl}`);

  if (finalUrl.includes('/dashboard')) {
    console.log('✅ SUCCESS! Redirected to dashboard');

    const kpis = await page.locator('[class*="kpi"]').all();
    console.log(`   Found ${kpis.length} KPI cards`);

    // Show dashboard content
    const bodyText = await page.locator('body').textContent();
    console.log(`   Body (${bodyText.length} chars): ${bodyText.substring(0, 200)}...`);
  } else {
    console.log(`❌ NOT on dashboard — still at: ${finalUrl}`);

    // Check for error messages
    try {
      const errors = await page.locator('.error').all();
      if (errors.length > 0) {
        for (const e of errors) {
          console.log(`   Error: ${await e.textContent()}`);
        }
      }
    } catch {}

    // Show body
    const bodyText = await page.locator('body').textContent();
    console.log(`   Body (${bodyText.length} chars): ${bodyText.substring(0, 400)}`);
  }

  console.log('\n=== API Call Log ===');
  for (const entry of apiLog) {
    console.log(entry);
  }

  await page.screenshot({ path: 'e2e/login-test-result.png', fullPage: true });
});
