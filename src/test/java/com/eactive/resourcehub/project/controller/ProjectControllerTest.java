package com.eactive.resourcehub.project.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.common.security.CustomUserDetailsService;
import com.eactive.resourcehub.project.entity.Project;
import com.eactive.resourcehub.project.service.ProjectService;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProjectController API/슬라이스 테스트.
 * HTTP 상태코드, CSRF 보호, 리다이렉트 경로를 검증한다.
 */
@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean ProjectService           projectService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    private static final LocalDate TODAY = LocalDate.now();

    private User adminUser;
    private User salesUser;

    @BeforeEach
    void setUp() {
        adminUser = makeUser(1L, UserRole.ADMIN);
        salesUser = makeUser(2L, UserRole.SALES);

        // GET /sales/projects/{id} 기본 스텁
        Project project = makeProject(10L);
        when(projectService.findById(anyLong())).thenReturn(project);
        when(projectService.getMembersForProject(anyLong())).thenReturn(List.of());
        when(projectService.findAssignableUsers()).thenReturn(List.of());
    }

    // ── GET /sales/projects/{id} — 접근 제어 ─────────────────────────

    @Test
    void ADMIN_프로젝트_상세_페이지_접근_성공() throws Exception {
        mockMvc.perform(get("/sales/projects/10")
                        .with(user(new CustomUserDetails(adminUser))))
                .andExpect(status().isOk());
    }

    @Test
    void SALES_프로젝트_상세_페이지_접근_성공() throws Exception {
        mockMvc.perform(get("/sales/projects/10")
                        .with(user(new CustomUserDetails(salesUser))))
                .andExpect(status().isOk());
    }

    @Test
    void 미인증_프로젝트_상세_페이지_접근_거부() throws Exception {
        mockMvc.perform(get("/sales/projects/10"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 프로젝트_상세_페이지에_프로젝트명이_렌더링됨() throws Exception {
        mockMvc.perform(get("/sales/projects/10")
                        .with(user(new CustomUserDetails(adminUser))))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("테스트 프로젝트")));
    }

    // ── POST /admin/projects — CSRF 검증 ─────────────────────────────

    @Test
    void CSRF_없이_프로젝트_등록_요청_거부() throws Exception {
        mockMvc.perform(post("/admin/projects")
                        .with(user(new CustomUserDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    @Test
    void CSRF_없이_프로젝트_수정_요청_거부() throws Exception {
        mockMvc.perform(post("/admin/projects/10/update")
                        .with(user(new CustomUserDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    @Test
    void CSRF_없이_프로젝트_삭제_요청_거부() throws Exception {
        mockMvc.perform(post("/admin/projects/10/delete")
                        .with(user(new CustomUserDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    @Test
    void CSRF_없이_멤버_추가_요청_거부() throws Exception {
        mockMvc.perform(post("/admin/projects/10/members")
                        .with(user(new CustomUserDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    @Test
    void CSRF_없이_멤버_수정_요청_거부() throws Exception {
        mockMvc.perform(post("/admin/projects/10/members/20/update")
                        .with(user(new CustomUserDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    @Test
    void CSRF_없이_멤버_삭제_요청_거부() throws Exception {
        mockMvc.perform(post("/admin/projects/10/members/20/delete")
                        .with(user(new CustomUserDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    // ── POST /admin/projects — 성공 흐름 ─────────────────────────────

    @Test
    void ADMIN_CSRF_포함_프로젝트_등록_성공_후_상세_페이지_리다이렉트() throws Exception {
        Project saved = makeProject(10L);
        when(projectService.create(any(), any(), any(), any())).thenReturn(saved);

        mockMvc.perform(post("/admin/projects")
                        .with(user(new CustomUserDetails(adminUser)))
                        .with(csrf())
                        .param("name",      "신규 프로젝트")
                        .param("startDate", TODAY.toString())
                        .param("endDate",   TODAY.plusDays(30).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sales/projects/10"));
    }

    @Test
    void ADMIN_CSRF_포함_프로젝트_삭제_성공_후_캘린더_리다이렉트() throws Exception {
        mockMvc.perform(post("/admin/projects/10/delete")
                        .with(user(new CustomUserDetails(adminUser)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sales/calendar"));
    }

    @Test
    void ADMIN_CSRF_포함_멤버_삭제_성공_후_상세_페이지_리다이렉트() throws Exception {
        mockMvc.perform(post("/admin/projects/10/members/20/delete")
                        .with(user(new CustomUserDetails(adminUser)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sales/projects/10"));
    }

    // ── 헬퍼 ────────────────────────────────────────────────────────

    private User makeUser(long id, UserRole role) {
        User u = User.create("user" + id + "@test.co.kr", "encoded", "사용자" + id,
                "user" + id + "@test.co.kr", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(u, "id",   id);
        ReflectionTestUtils.setField(u, "role", role);
        return u;
    }

    private Project makeProject(long id) {
        Project p = Project.create("테스트 프로젝트", "테스트 고객사", TODAY, TODAY.plusDays(30), null);
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }
}
