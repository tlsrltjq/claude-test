package com.eactive.resourcehub.e2e;

import com.eactive.resourcehub.user.entity.AllowedEmail;
import com.eactive.resourcehub.user.repository.AllowedEmailRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * E2E 통합 테스트 — 단일 클래스로 컨텍스트·컨테이너를 한 번만 생성.
 * AdminInitializer가 admin@test.com / Test1234! 계정을 자동 생성.
 *
 * 커버리지: 인증, 관리자 기능, 문서/검색 기능
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("e2e")
class E2ETest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Autowired MockMvc mockMvc;
    @Autowired AllowedEmailRepository allowedEmailRepository;

    // ── 헬퍼 ─────────────────────────────────────────────────────

    private MockHttpSession loginAsAdmin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/login")
                        .session(session)
                        .param("username", "admin@test.com")
                        .param("password", "Test1234!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
        return session;
    }

    // ══════════════════════════════════════════════════════════════
    // 인증 (Auth)
    // ══════════════════════════════════════════════════════════════

    @Test
    void GET_login_페이지_미인증으로_접근_가능() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void GET_signup_페이지_미인증으로_접근_가능() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk());
    }

    @Test
    void 미인증_search_접근시_login으로_리다이렉트() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void 미인증_admin_접근시_login으로_리다이렉트() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void 미인증_my_folder_접근시_login으로_리다이렉트() throws Exception {
        mockMvc.perform(get("/my/folder"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void 올바른_자격증명으로_로그인_성공하면_대시보드로_리다이렉트() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/login")
                        .session(session)
                        .param("username", "admin@test.com")
                        .param("password", "Test1234!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void 잘못된_비밀번호로_로그인_실패하면_error_파라미터() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin@test.com")
                        .param("password", "WrongPassword!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void 존재하지_않는_계정으로_로그인_실패() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "nobody@test.com")
                        .param("password", "Test1234!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void 로그아웃_후_login_logout_리다이렉트() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    // ══════════════════════════════════════════════════════════════
    // 관리자 (Admin)
    // ══════════════════════════════════════════════════════════════

    @Test
    void 관리자_대시보드_접근_200() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void EMPLOYEE권한으로_admin_접근시_403() throws Exception {
        allowedEmailRepository.save(AllowedEmail.create("employee@e2e.com", "e2e test", null));

        // 회원가입 1단계
        MockHttpSession signupSession = new MockHttpSession();
        mockMvc.perform(post("/signup")
                        .session(signupSession)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "테스트직원")
                        .param("birthDateStr", "19900101")
                        .param("phone", "010-1111-2222")
                        .param("email", "employee@e2e.com")
                        .param("position", "STAFF")
                        .param("password", "Test1234!")
                        .param("passwordConfirm", "Test1234!")
                        .param("privacyConsent", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        String code = (String) signupSession.getAttribute("PENDING_SIGNUP_CODE");
        if (code == null) return;

        // 회원가입 2단계
        mockMvc.perform(post("/signup/verify")
                        .session(signupSession)
                        .param("code", code)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // EMPLOYEE로 로그인
        MockHttpSession empSession = new MockHttpSession();
        mockMvc.perform(post("/login")
                        .session(empSession)
                        .param("username", "employee@e2e.com")
                        .param("password", "Test1234!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/admin/employees").session(empSession))
                .andExpect(status().isForbidden());
    }

    @Test
    void 직원_목록_페이지_200() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/admin/employees").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void 직원_목록_키워드_검색_200() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/admin/employees").session(session).param("q", "관리자"))
                .andExpect(status().isOk());
    }

    @Test
    void 허용_이메일_목록_200() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/admin/allowed-emails").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void 허용_이메일_추가_성공_redirect() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(post("/admin/allowed-emails")
                        .session(session)
                        .param("email", "newuser@e2e.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void 문서_검토_목록_200() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/admin/documents/review").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void 관리자_통계_200() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/admin/statistics").session(session))
                .andExpect(status().isOk());
    }

    // ══════════════════════════════════════════════════════════════
    // 문서 / 검색 (Document)
    // ══════════════════════════════════════════════════════════════

    @Test
    void 검색_페이지_200() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/search").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void 검색_키워드_파라미터_200() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/search").session(session).param("keyword", "이력서"))
                .andExpect(status().isOk());
    }

    @Test
    void 검색_날짜_범위_파라미터_200() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/search").session(session)
                        .param("dateFrom", "2024-01-01")
                        .param("dateTo", "2024-12-31"))
                .andExpect(status().isOk());
    }

    @Test
    void 개인폴더_페이지_200() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/my/folder").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void 개인폴더_업로드_폼_200() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/my/folder/documents/upload").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void 공용폴더_페이지_200() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/shared/folders/public").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void 대시보드_200() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk());
    }
}
