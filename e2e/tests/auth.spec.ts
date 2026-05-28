import { test, expect, Page } from '@playwright/test';

const ADMIN_EMAIL = 'admin@eactive.co.kr';
const ADMIN_PW    = 'Admin1234!';
const BASE        = 'http://localhost:8080';

async function login(page: Page, email: string, pw: string) {
  await page.goto('/login');
  await page.fill('input[name="username"]', email);
  await page.fill('input[name="password"]', pw);
  await page.click('button[type="submit"]');
}

// ── 공개 페이지 ────────────────────────────────────────────────────
test.describe('공개 페이지', () => {
  test('로그인 페이지 — app.css 로드 + 핵심 요소', async ({ page }) => {
    await page.goto('/login');
    // CSS 링크 존재 여부
    const cssLink = page.locator('link[href*="app.css"]');
    await expect(cssLink).toHaveCount(1);
    // 인라인 <style> 태그 없음
    const styleTags = page.locator('style');
    await expect(styleTags).toHaveCount(0);
    // 로그인 폼 요소
    await expect(page.locator('input[name="username"]')).toBeVisible();
    await expect(page.locator('input[name="password"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
    // 좌측 브랜딩 패널 렌더링
    await expect(page.locator('.auth-left')).toBeVisible();
  });

  test('비밀번호 찾기 페이지 렌더링', async ({ page }) => {
    await page.goto('/login/forgot');
    await expect(page.locator('.auth-header')).toBeVisible();
    await expect(page.locator('input[name="email"]')).toBeVisible();
  });

  test('미인증 상태에서 admin 페이지 접근 → 로그인 리다이렉트', async ({ page }) => {
    // 로그아웃 상태에서 admin 페이지 접근 → /login 으로 리다이렉트
    await page.goto('/admin');
    await expect(page).toHaveURL(/\/login/);
  });
});

// ── 어드민 인증 흐름 ───────────────────────────────────────────────
test.describe('어드민 흐름', () => {
  test('관리자 로그인 → 대시보드 리다이렉트', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await expect(page).toHaveURL(/\/(admin|dashboard)/);
  });

  test('관리자 대시보드 — 핵심 카드 노출', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/admin');
    // 사이드바
    await expect(page.locator('.app-sidebar')).toBeVisible();
    // KPI 카드
    await expect(page.locator('.kpi-card').first()).toBeVisible();
    // 빠른 메뉴
    await expect(page.locator('.menu-list-item').first()).toBeVisible();
    // 인라인 style 없음
    await expect(page.locator('style')).toHaveCount(0);
    // 페이지 제목 포함
    await expect(page).toHaveTitle(/관리자 대시보드/);
  });

  test('직원 목록 페이지 — 테이블 헤더 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/admin/employees');
    await expect(page.locator('table thead th').first()).toBeVisible();
  });

  test('팀 관리 페이지 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/admin/teams');
    await expect(page.locator('.page-header')).toBeVisible();
    await expect(page.locator('style')).toHaveCount(0);
  });

  test('통계 페이지 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/admin/statistics');
    await expect(page.locator('.page-header')).toBeVisible();
    await expect(page.locator('style')).toHaveCount(0);
  });

  test('프로젝트 설정 페이지 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/admin/teams/project-settings');
    await expect(page.locator('.page-header')).toBeVisible();
  });

  test('허용 이메일 페이지 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/admin/allowed-emails');
    await expect(page.locator('.page-header')).toBeVisible();
  });
});

// ── 영업 흐름 ─────────────────────────────────────────────────────
test.describe('영업(Sales) 흐름', () => {
  test('영업 캘린더 페이지 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/sales/calendar');
    await expect(page.locator('.topnav')).toBeVisible();
    await expect(page.locator('.cal-table')).toBeVisible();
    await expect(page.locator('style')).toHaveCount(0);
  });

  test('인력표 페이지 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/sales/members');
    await expect(page.locator('.members-card, .empty-state')).toBeVisible();
    await expect(page.locator('style')).toHaveCount(0);
  });

  test('경력 계산기 페이지 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/sales/career-calculator');
    await expect(page.locator('.calc-card').first()).toBeVisible();
    await expect(page.locator('style')).toHaveCount(0);
  });

  test('인력표(프로필) 페이지 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/sales/profiles');
    await expect(page.locator('.topnav')).toBeVisible();
    await expect(page.locator('style')).toHaveCount(0);
  });
});

// ── 일반 사용자 흐름 ──────────────────────────────────────────────
test.describe('내 화면(My) 흐름', () => {
  test('대시보드 — welcome-banner 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/dashboard');
    await expect(page.locator('.app-sidebar')).toBeVisible();
    await expect(page.locator('.welcome-banner')).toBeVisible();
    await expect(page.locator('style')).toHaveCount(0);
  });

  test('내 폴더 페이지 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/my/folder');
    await expect(page.locator('.topnav')).toBeVisible();
    await expect(page.locator('style')).toHaveCount(0);
  });

  test('설정 페이지 — 탭 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/settings');
    await expect(page.locator('.settings-card')).toBeVisible();
    await expect(page.locator('.nav-tabs-custom')).toBeVisible();
    await expect(page.locator('style')).toHaveCount(0);
  });

  test('활동 이력 페이지 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/my/activity');
    await expect(page.locator('.page-header')).toBeVisible();
    await expect(page.locator('style')).toHaveCount(0);
  });

  test('공용 폴더 페이지 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/shared/folders/public');
    await expect(page.locator('.topnav')).toBeVisible();
    await expect(page.locator('style')).toHaveCount(0);
  });

  test('검색 페이지 렌더링', async ({ page }) => {
    await login(page, ADMIN_EMAIL, ADMIN_PW);
    await page.goto('/search');
    await expect(page.locator('.search-card')).toBeVisible();
    await expect(page.locator('style')).toHaveCount(0);
  });
});

// ── 로그아웃 ──────────────────────────────────────────────────────
test('로그아웃 → 로그인 페이지 리다이렉트', async ({ page }) => {
  await login(page, ADMIN_EMAIL, ADMIN_PW);
  await page.goto('/dashboard');
  // CSRF 토큰과 함께 POST /logout
  await page.evaluate(() => {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/logout';
    const csrf = document.querySelector('input[name="_csrf"]') as HTMLInputElement;
    if (csrf) {
      const input = document.createElement('input');
      input.type = 'hidden';
      input.name = '_csrf';
      input.value = csrf.value;
      form.appendChild(input);
    }
    document.body.appendChild(form);
    form.submit();
  });
  await page.waitForURL(/\/login/);
  await expect(page).toHaveURL(/\/login/);
});
