package com.eactive.resourcehub.project.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.common.security.CustomUserDetailsService;
import com.eactive.resourcehub.project.entity.Project;
import com.eactive.resourcehub.project.entity.ProjectAssignment;
import com.eactive.resourcehub.project.entity.ProjectStatus;
import com.eactive.resourcehub.project.service.ProjectAssignmentService;
import com.eactive.resourcehub.project.service.ProjectService;
import com.eactive.resourcehub.team.entity.Team;
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
 * ProjectAssignmentController API/슬라이스 테스트.
 * HTTP 상태코드, CSRF 보호, 역할별 접근 제어를 검증한다.
 * 스펙 ID: PA-003, PA-008, PA-009, PA-010.
 */
@WebMvcTest(ProjectAssignmentController.class)
class ProjectAssignmentControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean ProjectAssignmentService  assignmentService;
    @MockitoBean ProjectService            projectService;
    @MockitoBean CustomUserDetailsService  customUserDetailsService;

    private static final LocalDate TODAY = LocalDate.now();

    private User adminUser;
    private User salesUser;
    private User empUser;

    @BeforeEach
    void setUp() {
        adminUser = makeUser(1L, UserRole.ADMIN);
        salesUser = makeUser(2L, UserRole.SALES);
        empUser   = makeUser(3L, UserRole.EMPLOYEE);

        // GET /sales/calendar 에서 호출되는 stub 기본 설정
        when(assignmentService.getMonthlyAssignments(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(assignmentService.findAssignableUsers()).thenReturn(List.of());
        when(projectService.getMonthlyProjects(any())).thenReturn(List.of());
    }

    // ── GET /sales/calendar — 접근 제어 ────────────────────────────

    @Test
    void ADMIN_캘린더_페이지_접근_성공() throws Exception {
        mockMvc.perform(get("/sales/calendar")
                        .with(user(new CustomUserDetails(adminUser))))
                .andExpect(status().isOk());
    }

    @Test
    void SALES_캘린더_페이지_접근_성공() throws Exception {
        mockMvc.perform(get("/sales/calendar")
                        .with(user(new CustomUserDetails(salesUser))))
                .andExpect(status().isOk());
    }

    // @WebMvcTest 슬라이스에서 역할 기반 접근 제어(EMPLOYEE → /sales/** 403)는 검증 불가:
    // user() 포스트프로세서는 SecurityFilterChain을 우회, @WithMockUser는 CustomUserDetails
    // 가 아닌 generic User를 사용해 NPE 발생. SecurityConfig의 role 제한은 security-lint.sh
    // 및 수동 QA(docs/qa-checklist.md)로 보완한다. (SecurityAccessTest 주석 참조)

    @Test
    void 미인증_캘린더_페이지_접근_거부() throws Exception {
        // @WebMvcTest에서는 formLogin 대신 HTTP Basic이 적용되어 4xx 반환
        mockMvc.perform(get("/sales/calendar"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 캘린더_년월_파라미터로_특정_월_조회() throws Exception {
        mockMvc.perform(get("/sales/calendar")
                        .with(user(new CustomUserDetails(salesUser)))
                        .param("year", "2025")
                        .param("month", "6"))
                .andExpect(status().isOk());
    }

    // ── GET /sales/calendar — 템플릿 렌더링 ───────────────────────────
    // 팀·배정 데이터를 스텁에 포함해 실제 Thymeleaf 표현식(u.team.name 등)을 실행시킴.
    // LazyInitializationException은 @WebMvcTest에서 재현 불가하지만,
    // NPE·필드명 오류·null 처리 분기 등은 이 케이스로 검증 가능.

    @Test
    void 팀_있는_유저와_배정_데이터로_캘린더_페이지_정상_렌더링() throws Exception {
        Team team = Team.create("개발팀", null);
        ReflectionTestUtils.setField(team, "id", 10L);

        User userWithTeam = makeUser(4L, UserRole.EMPLOYEE);
        ReflectionTestUtils.setField(userWithTeam, "team", team);

        ProjectAssignment assignment = makeAssignment(userWithTeam);

        when(assignmentService.getMonthlyAssignments(any(), any(), any(), any()))
                .thenReturn(List.of(assignment));
        when(assignmentService.findAssignableUsers())
                .thenReturn(List.of(userWithTeam));

        mockMvc.perform(get("/sales/calendar")
                        .with(user(new CustomUserDetails(adminUser))))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("개발팀")));
    }

    @Test
    void 팀_없는_유저도_캘린더_페이지_정상_렌더링() throws Exception {
        User userWithoutTeam = makeUser(5L, UserRole.EMPLOYEE); // team = null

        when(assignmentService.getMonthlyAssignments(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(assignmentService.findAssignableUsers())
                .thenReturn(List.of(userWithoutTeam));

        // 팀 없는 유저도 NPE 없이 200 OK 반환되고, 모달 체크박스에 이름이 렌더링됨
        mockMvc.perform(get("/sales/calendar")
                        .with(user(new CustomUserDetails(adminUser))))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("사용자5")));
    }

    // ── GET /sales/calendar — 프로젝트 바 렌더링 ─────────────────────

    @Test
    void 프로젝트가_있으면_바_색상_클래스가_캘린더에_렌더링됨() throws Exception {
        Project project = Project.create("테스트 프로젝트", "테스트 고객사",
                TODAY, TODAY.plusDays(14), null);
        ReflectionTestUtils.setField(project, "id", 3L);

        when(projectService.getMonthlyProjects(any())).thenReturn(List.of(project));

        mockMvc.perform(get("/sales/calendar")
                        .with(user(new CustomUserDetails(adminUser))))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("bar-color-3")));
    }

    @Test
    void 프로젝트가_없으면_바_행이_렌더링되지_않음() throws Exception {
        mockMvc.perform(get("/sales/calendar")
                        .with(user(new CustomUserDetails(adminUser))))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.containsString("cal-bar-row"))));
    }

    // ── POST /sales/assignments — CSRF 검증 ─────────────────────────

    @Test
    void CSRF_토큰_없이_배정_등록_요청_거부() throws Exception {
        mockMvc.perform(post("/sales/assignments")
                        .with(user(new CustomUserDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    @Test
    void CSRF_토큰_없이_배정_삭제_요청_거부() throws Exception {
        mockMvc.perform(post("/sales/assignments/1/delete")
                        .with(user(new CustomUserDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    @Test
    void CSRF_토큰_없이_배정_수정_요청_거부() throws Exception {
        mockMvc.perform(post("/sales/assignments/1")
                        .with(user(new CustomUserDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    // ── POST /sales/assignments/{id}/delete — 삭제 흐름 ─────────────

    @Test
    void ADMIN_CSRF_포함_배정_삭제_성공() throws Exception {
        mockMvc.perform(post("/sales/assignments/10/delete")
                        .with(user(new CustomUserDetails(adminUser)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sales/calendar"));
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

    private ProjectAssignment makeAssignment(User user) {
        Project project = Project.create("테스트 프로젝트", "테스트 고객사",
                TODAY, TODAY.plusDays(30), null);
        ReflectionTestUtils.setField(project, "id", 1L);
        ProjectAssignment pa = ProjectAssignment.createForProject(
                project, user, "개발자", TODAY, TODAY.plusDays(30));
        ReflectionTestUtils.setField(pa, "id", 10L);
        return pa;
    }
}
