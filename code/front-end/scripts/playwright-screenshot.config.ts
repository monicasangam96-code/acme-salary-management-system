import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './scripts',
  testMatch: '**/capture-screenshots.ts',
  timeout: 60 * 1000,
  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:4200',
    trace: 'on-first-retry',
    viewport: { width: 1280, height: 800 },
  },
  reporter: 'list',
});
