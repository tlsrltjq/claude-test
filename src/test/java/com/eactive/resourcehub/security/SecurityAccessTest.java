package com.eactive.resourcehub.security;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.common.security.CustomUserDetailsService;
import com.eactive.resourcehub.employee.service.CareerSaveService;
import com.eactive.resourcehub.project.service.ProjectAssignmentService;
import com.eactive.resourcehub.user.controller.DashboardController;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.user.service.SettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class SecurityAccessTest {

    @Autowired MockMvc mockMvc;

    @MockBean SettingsService settingsService;
    @MockBean CareerSaveService careerSaveService;
    @MockBean ProjectAssignmentService projectAssignmentService;
    @MockBean CustomUserDetailsService customUserDetailsService;

    // ── 미인증 접근 ─────────────────────────────────────────────
    // @WebMvcTest 슬라이스에서는 formLogin 대신 HTTP Basic이 적용되어 401 반환.
    // 실제 운영에서는 /login 으로 302 리다이렉트 (통합 테스트 또는 수동 QA로 검증).

    @Test
    void 미인증_dashboard_접근_거부() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 로그인_페이지는_미인증_접근_허용() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    // ── 역할별 dashboard 접근 ────────────────────────────────────

    @Test
    void EMPLOYEE_dashboard_접근_성공() throws Exception {
        User u = makeUser(1L, UserRole.EMPLOYEE);
        when(settingsService.getUser(1L)).thenReturn(u);
        when(careerSaveService.findProfile(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/dashboard")
                        .with(user(new CustomUserDetails(u))))
                .andExpect(status().isOk());
    }

    @Test
    void ADMIN_dashboard_접근_성공() throws Exception {
        User u = makeUser(2L, UserRole.ADMIN);
        when(settingsService.getUser(2L)).thenReturn(u);
        when(careerSaveService.findProfile(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/dashboard")
                        .with(user(new CustomUserDetails(u))))
                .andExpect(status().isOk());
    }

    @Test
    void SALES_dashboard_접근_성공() throws Exception {
        User u = makeUser(3L, UserRole.SALES);
        when(settingsService.getUser(3L)).thenReturn(u);
        when(careerSaveService.findProfile(3L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/dashboard")
                        .with(user(new CustomUserDetails(u))))
                .andExpect(status().isOk());
    }

    // ── 헬퍼 ────────────────────────────────────────────────────

    private User makeUser(long id, UserRole role) {
        User u = User.create(
                "user" + id, "encoded", "테스트" + id,
                "user" + id + "@eactive.co.kr", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000"
        );
        ReflectionTestUtils.setField(u, "id", id);
        if (role != UserRole.EMPLOYEE) {
            ReflectionTestUtils.setField(u, "role", role);
        }
        return u;
    }
}
