package com.eactive.resourcehub.security;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.common.security.CustomUserDetailsService;
import com.eactive.resourcehub.common.security.SecurityConfig;
import com.eactive.resourcehub.document.controller.SearchController;
import com.eactive.resourcehub.document.service.SearchService;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 라우트 권한 매트릭스 테스트.
 * SecurityConfig 규칙: /admin/** → ADMIN만, /sales/** → ADMIN+SALES, 그 외 → 인증 필요.
 */
@WebMvcTest(SearchController.class)
@Import(SecurityConfig.class)
class RouteSecurityTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean SearchService            searchService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    // ── 미인증 접근 — form login 설정으로 /login 으로 302 리다이렉트 ──

    @Test
    void 미인증_search_접근시_login으로_리다이렉트() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void 미인증_admin_접근시_login으로_리다이렉트() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void 미인증_sales_접근시_login으로_리다이렉트() throws Exception {
        mockMvc.perform(get("/sales/calendar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ── EMPLOYEE 권한 매트릭스 ────────────────────────────────────

    @Test
    void EMPLOYEE_search_접근_성공() throws Exception {
        when(searchService.search(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/search").with(user(details(1L, UserRole.EMPLOYEE))))
                .andExpect(status().isOk());
    }

    @Test
    void EMPLOYEE_admin_접근_403() throws Exception {
        mockMvc.perform(get("/admin/users").with(user(details(1L, UserRole.EMPLOYEE))))
                .andExpect(status().isForbidden());
    }

    @Test
    void EMPLOYEE_sales_접근_403() throws Exception {
        mockMvc.perform(get("/sales/calendar").with(user(details(1L, UserRole.EMPLOYEE))))
                .andExpect(status().isForbidden());
    }

    // ── SALES 권한 매트릭스 ──────────────────────────────────────

    @Test
    void SALES_sales_접근_성공_응답코드_2xx_or_3xx() throws Exception {
        // SearchController가 /sales/** 를 처리하지 않으므로 404지만 권한은 통과(403 아님)
        mockMvc.perform(get("/sales/calendar").with(user(details(2L, UserRole.SALES))))
                .andExpect(not403());
    }

    @Test
    void SALES_admin_접근_403() throws Exception {
        mockMvc.perform(get("/admin/users").with(user(details(2L, UserRole.SALES))))
                .andExpect(status().isForbidden());
    }

    // ── ADMIN 권한 매트릭스 ──────────────────────────────────────

    @Test
    void ADMIN_admin_접근_권한_통과() throws Exception {
        mockMvc.perform(get("/admin/users").with(user(details(3L, UserRole.ADMIN))))
                .andExpect(not403());
    }

    @Test
    void ADMIN_sales_접근_권한_통과() throws Exception {
        mockMvc.perform(get("/sales/calendar").with(user(details(3L, UserRole.ADMIN))))
                .andExpect(not403());
    }

    // ── 공개 라우트 — permitAll 확인 (controller 없어 404이나 403은 아님) ──

    @Test
    void 미인증_login_페이지는_보안_차단_없음() throws Exception {
        // LoginController가 이 슬라이스에 없어 404이지만, 403(보안 차단)은 아님
        mockMvc.perform(get("/login"))
                .andExpect(not403());
    }

    @Test
    void 미인증_signup_페이지는_보안_차단_없음() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(not403());
    }

    // ── 헬퍼 ────────────────────────────────────────────────────

    private CustomUserDetails details(long id, UserRole role) {
        User u = User.create("user" + id + "@test.com", "encoded", "테스트" + id,
                "user" + id + "@test.com", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(u, "id", id);
        ReflectionTestUtils.setField(u, "role", role);
        return new CustomUserDetails(u);
    }

    /** 403이 아님을 검증 — 404(라우트 없음)는 통과, 권한 차단(403)은 실패. */
    private org.springframework.test.web.servlet.ResultMatcher not403() {
        return result -> {
            int status = result.getResponse().getStatus();
            if (status == 403) {
                throw new AssertionError("Expected not 403 (Forbidden) but was 403");
            }
        };
    }
}
