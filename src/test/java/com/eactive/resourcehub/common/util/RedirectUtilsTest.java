package com.eactive.resourcehub.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedirectUtilsTest {

    // ── null / 빈 값 ──────────────────────────────────────────

    @Test
    void null_referer는_fallback_반환() {
        assertThat(RedirectUtils.safeReferer(null, "/fallback")).isEqualTo("/fallback");
    }

    @Test
    void 빈_referer는_fallback_반환() {
        assertThat(RedirectUtils.safeReferer("", "/fallback")).isEqualTo("/fallback");
        assertThat(RedirectUtils.safeReferer("   ", "/fallback")).isEqualTo("/fallback");
    }

    // ── 상대 경로 — 그대로 통과 ───────────────────────────────

    @Test
    void 슬래시로_시작하는_상대경로는_그대로_반환() {
        assertThat(RedirectUtils.safeReferer("/my/folder", "/fallback")).isEqualTo("/my/folder");
    }

    @Test
    void 쿼리스트링_포함_상대경로도_그대로_반환() {
        assertThat(RedirectUtils.safeReferer("/admin/employees?tab=info", "/fallback"))
                .isEqualTo("/admin/employees?tab=info");
    }

    // ── protocol-relative URL open redirect 방어 ─────────────

    @Test
    void 슬래시_두개로_시작하면_경로만_추출() {
        // //evil.com/hack → URI 파싱 후 path(/hack)만 추출 → 같은 서버 경로로 안전
        assertThat(RedirectUtils.safeReferer("//evil.com/hack", "/fallback")).isEqualTo("/hack");
    }

    // ── 절대 URL — 경로만 추출 ────────────────────────────────

    @Test
    void 절대_URL은_경로만_추출() {
        assertThat(RedirectUtils.safeReferer("https://eactive.co.kr/my/folder", "/fallback"))
                .isEqualTo("/my/folder");
    }

    @Test
    void 절대_URL_쿼리스트링_포함하면_경로와_쿼리만_반환() {
        assertThat(RedirectUtils.safeReferer("https://eactive.co.kr/admin/employees?tab=info", "/fallback"))
                .isEqualTo("/admin/employees?tab=info");
    }

    @Test
    void 외부_도메인_절대_URL은_경로와_쿼리만_추출해_open_redirect_방지() {
        // 외부 도메인이라도 path+query만 추출 → 같은 서버 상대 경로로 한정
        assertThat(RedirectUtils.safeReferer("https://evil.com/steal?token=abc", "/fallback"))
                .isEqualTo("/steal?token=abc");
    }

    // ── 경로가 비어있는 절대 URL ──────────────────────────────

    @Test
    void 경로_없는_절대_URL은_fallback_반환() {
        assertThat(RedirectUtils.safeReferer("https://eactive.co.kr", "/fallback")).isEqualTo("/fallback");
    }

    // ── 잘못된 URI ────────────────────────────────────────────

    @Test
    void 잘못된_URI는_fallback_반환() {
        assertThat(RedirectUtils.safeReferer("not a valid uri %%", "/fallback")).isEqualTo("/fallback");
    }
}
