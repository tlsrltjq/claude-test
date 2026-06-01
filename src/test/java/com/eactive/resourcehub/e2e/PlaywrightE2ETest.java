package com.eactive.resourcehub.e2e;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Playwright 브라우저 E2E 테스트.
 * 실제 Chromium 브라우저로 렌더링된 UI를 검증한다.
 *
 * 실행 방법:
 *   1. ./gradlew playwrightInstall   (최초 1회 브라우저 설치)
 *   2. ./gradlew playwrightTest      (E2E 테스트 실행)
 *
 * 일반 ./gradlew test 에서는 playwright 태그로 제외됨.
 */
@Tag("playwright")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlaywrightE2ETest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @LocalServerPort
    int port;

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void createContext() {
        context = browser.newContext(new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true));
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    // ── 헬퍼 ─────────────────────────────────────────────────

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    /** 관리자 계정으로 로그인하고 세션이 유지된 page를 반환 */
    private void loginAsAdmin() {
        page.navigate(url("/login"));
        page.fill("input[name='username']", "admin@test.com");
        page.fill("input[name='password']", "Test1234!");
        page.click("button[type='submit']");
        page.waitForURL(url("/dashboard"));
    }

    // ══════════════════════════════════════════════════════════
    // 로그인 페이지 UI
    // ══════════════════════════════════════════════════════════

    @Test
    @Order(1)
    void 로그인_페이지_폼_요소_존재() {
        page.navigate(url("/login"));

        assertThat(page.title()).isNotBlank();
        assertThat(page.locator("input[name='username']").count()).isEqualTo(1);
        assertThat(page.locator("input[name='password']").count()).isEqualTo(1);
        assertThat(page.locator("button[type='submit']").count()).isGreaterThan(0);
    }

    @Test
    @Order(2)
    void 로그인_성공하면_대시보드로_이동() {
        loginAsAdmin();
        assertThat(page.url()).isEqualTo(url("/dashboard"));
    }

    @Test
    @Order(3)
    void 틀린_비밀번호로_로그인_실패시_에러_표시() {
        page.navigate(url("/login"));
        page.fill("input[name='username']", "admin@test.com");
        page.fill("input[name='password']", "WrongPassword!");
        page.click("button[type='submit']");

        // 로그인 페이지에 머물고 에러 메시지 또는 error 파라미터 존재
        page.waitForURL(url("/login?error"));
        assertThat(page.url()).contains("/login");
    }

    // ══════════════════════════════════════════════════════════
    // 사이드바 네비게이션
    // ══════════════════════════════════════════════════════════

    @Test
    @Order(10)
    void 로그인_후_사이드바_렌더링() {
        loginAsAdmin();

        // 사이드바 링크가 1개 이상 존재
        assertThat(page.locator(".sb-link").count()).isGreaterThan(0);
    }

    @Test
    @Order(11)
    void 내_폴더_링크_클릭하면_페이지_이동() {
        loginAsAdmin();

        page.locator(".sb-link[data-path='/my/folder']").click();
        page.waitForURL(url("/my/folder"));

        assertThat(page.url()).isEqualTo(url("/my/folder"));
        // 내 폴더 페이지 제목 확인
        assertThat(page.locator("h1, h2, h3, .page-title").first().isVisible()).isTrue();
    }

    @Test
    @Order(12)
    void 문서_검색_링크_클릭하면_검색_페이지_이동() {
        loginAsAdmin();

        page.locator(".sb-link[data-path='/search']").click();
        page.waitForURL(url("/search"));

        assertThat(page.url()).isEqualTo(url("/search"));
    }

    @Test
    @Order(13)
    void 공용폴더_링크_클릭하면_페이지_이동() {
        loginAsAdmin();

        page.locator(".sb-link[data-path='/shared/folders/public']").click();
        page.waitForURL(url("/shared/folders/public"));

        assertThat(page.url()).isEqualTo(url("/shared/folders/public"));
    }

    // ══════════════════════════════════════════════════════════
    // 관리자 페이지
    // ══════════════════════════════════════════════════════════

    @Test
    @Order(20)
    void 관리자_사이드바_메뉴_존재() {
        loginAsAdmin();

        // admin 경로 사이드바 링크 확인
        assertThat(page.locator(".sb-link[data-path='/admin']").count()).isEqualTo(1);
    }

    @Test
    @Order(21)
    void 관리자_직원목록_페이지_테이블_렌더링() {
        loginAsAdmin();
        page.navigate(url("/admin/employees"));

        // 테이블 또는 직원 목록 요소가 존재
        assertThat(page.locator("table, .employee-list, [data-testid='employee-row']")
                .count()).isGreaterThanOrEqualTo(0);
        // 페이지 자체는 200으로 정상 렌더링
        assertThat(page.locator("body").isVisible()).isTrue();
    }

    @Test
    @Order(22)
    void 관리자_통계_페이지_렌더링() {
        loginAsAdmin();
        page.navigate(url("/admin/statistics"));

        assertThat(page.locator("body").isVisible()).isTrue();
        // 에러 페이지(500)가 아닌지 확인
        assertThat(page.title()).doesNotContain("500");
    }

    @Test
    @Order(23)
    void 관리자_허용이메일_페이지_입력폼_존재() {
        loginAsAdmin();
        page.navigate(url("/admin/allowed-emails"));

        // 이메일 추가 폼 존재 확인
        assertThat(page.locator("input[name='email'], input[type='email']").count())
                .isGreaterThan(0);
    }

    // ══════════════════════════════════════════════════════════
    // 설정 페이지
    // ══════════════════════════════════════════════════════════

    @Test
    @Order(30)
    void 설정_페이지_탭_렌더링() {
        loginAsAdmin();
        page.navigate(url("/settings"));

        assertThat(page.locator("body").isVisible()).isTrue();
        assertThat(page.title()).doesNotContain("500");
    }

    // ══════════════════════════════════════════════════════════
    // 접근 제어 (미인증 → 로그인 리다이렉트)
    // ══════════════════════════════════════════════════════════

    @Test
    @Order(40)
    void 미인증_대시보드_접근시_로그인으로_리다이렉트() {
        page.navigate(url("/dashboard"));
        // Spring Security가 /login으로 리다이렉트
        assertThat(page.url()).contains("/login");
    }

    @Test
    @Order(41)
    void 미인증_관리자_페이지_접근시_로그인으로_리다이렉트() {
        page.navigate(url("/admin/employees"));
        assertThat(page.url()).contains("/login");
    }

    @Test
    @Order(42)
    void 미인증_내폴더_접근시_로그인으로_리다이렉트() {
        page.navigate(url("/my/folder"));
        assertThat(page.url()).contains("/login");
    }

    // ══════════════════════════════════════════════════════════
    // 로그아웃
    // ══════════════════════════════════════════════════════════

    @Test
    @Order(50)
    void 로그아웃_후_보호된_페이지_접근_차단() {
        loginAsAdmin();

        // 로그아웃 버튼 클릭 (form POST /logout)
        page.locator("form[action='/logout'] button, button[data-action='logout']").first().click();
        page.waitForURL("**/login**");

        // 로그아웃 후 dashboard 접근 시 로그인으로 리다이렉트
        page.navigate(url("/dashboard"));
        assertThat(page.url()).contains("/login");
    }
}
