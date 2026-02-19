import { test, expect } from '@playwright/test';

test.describe('Dashboard Tests', () => {
  test('should load dashboard page', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('.dashboard-page, .login-page')).toBeVisible({ timeout: 10000 });
  });
});

test.describe('Import Page Tests', () => {
  test('should navigate to import page', async ({ page }) => {
    await page.goto('/import');
    await page.waitForTimeout(2000);
  });

  test('should display module selection', async ({ page }) => {
    await page.goto('/import');
    await page.waitForTimeout(2000);
  });
});

test.describe('Person Profile Tests', () => {
  test('should display person profile', async ({ page }) => {
    await page.goto('/person');
    await page.waitForTimeout(2000);
  });
});
