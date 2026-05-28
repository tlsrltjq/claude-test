package com.eactive.resourcehub.project.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.project.dto.ProjectCreateRequest;
import com.eactive.resourcehub.project.dto.ProjectMemberRequest;
import com.eactive.resourcehub.project.dto.ProjectUpdateRequest;
import com.eactive.resourcehub.project.entity.AssignmentStatus;
import com.eactive.resourcehub.project.entity.Project;
import com.eactive.resourcehub.project.entity.ProjectAssignment;
import com.eactive.resourcehub.project.entity.ProjectStatus;
import com.eactive.resourcehub.project.repository.ProjectAssignmentRepository;
import com.eactive.resourcehub.project.repository.ProjectRepository;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.user.entity.UserStatus;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProjectService 서비스 레이어 테스트.
 * CRUD 흐름, 권한 검증, 연쇄 삭제, 멤버 관리를 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectServiceTest {

    @Mock ProjectRepository          projectRepository;
    @Mock ProjectAssignmentRepository assignmentRepository;
    @Mock UserRepository             userRepository;
    @Mock AuditService               auditService;
    @Mock HttpServletRequest         httpReq;

    @InjectMocks ProjectService service;

    private static final LocalDate TODAY = LocalDate.now();

    private User adminUser;
    private User salesUser;
    private User empUser;
    private User disabledUser;

    @BeforeEach
    void setUp() {
        adminUser   = makeUser(1L, UserRole.ADMIN,    UserStatus.ACTIVE);
        salesUser   = makeUser(2L, UserRole.SALES,    UserStatus.ACTIVE);
        empUser     = makeUser(3L, UserRole.EMPLOYEE, UserStatus.ACTIVE);
        disabledUser = makeUser(4L, UserRole.EMPLOYEE, UserStatus.DISABLED);
    }

    // ── create ───────────────────────────────────────────────────────

    @Test
    void ADMIN_프로젝트_생성하면_저장되고_감사_로그_기록() {
        Project saved = makeProject(10L);
        when(projectRepository.save(any())).thenReturn(saved);

        Project result = service.create(validCreateReq(null), adminUser.getId(), UserRole.ADMIN, httpReq);

        assertNotNull(result);
        verify(projectRepository).save(any(Project.class));
        verify(auditService).log(eq(adminUser.getId()),
                eq(AuditActionType.CREATE_PROJECT),
                eq(AuditTargetType.PROJECT),
                eq(10L), anyString(), eq(httpReq));
    }

    @Test
    void ADMIN이_선택_직원과_함께_프로젝트_생성하면_배정도_저장() {
        Project saved = makeProject(10L);
        when(projectRepository.save(any())).thenReturn(saved);
        when(userRepository.findById(empUser.getId())).thenReturn(Optional.of(empUser));
        when(assignmentRepository.save(any())).thenReturn(makeAssignment(empUser, saved));

        ProjectCreateRequest req = validCreateReq(List.of(empUser.getId()));
        service.create(req, adminUser.getId(), UserRole.ADMIN, httpReq);

        verify(assignmentRepository).save(any(ProjectAssignment.class));
    }

    @Test
    void ADMIN이_아니면_프로젝트_생성_시_403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(validCreateReq(null), salesUser.getId(), UserRole.SALES, httpReq));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(projectRepository, never()).save(any());
    }

    @Test
    void 비활성화_직원과_프로젝트_생성_시_예외() {
        Project saved = makeProject(10L);
        when(projectRepository.save(any())).thenReturn(saved);
        when(userRepository.findById(disabledUser.getId())).thenReturn(Optional.of(disabledUser));

        assertThrows(IllegalArgumentException.class,
                () -> service.create(validCreateReq(List.of(disabledUser.getId())),
                        adminUser.getId(), UserRole.ADMIN, httpReq));
    }

    // ── update ───────────────────────────────────────────────────────

    @Test
    void ADMIN_프로젝트_수정하면_감사_로그_기록() {
        Project project = makeProject(10L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        service.update(10L, validUpdateReq(), adminUser.getId(), UserRole.ADMIN, httpReq);

        verify(auditService).log(eq(adminUser.getId()),
                eq(AuditActionType.UPDATE_PROJECT),
                eq(AuditTargetType.PROJECT),
                eq(10L), anyString(), eq(httpReq));
    }

    @Test
    void ADMIN이_아니면_프로젝트_수정_시_403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.update(10L, validUpdateReq(), salesUser.getId(), UserRole.SALES, httpReq));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void 존재하지_않는_프로젝트_수정_시_예외() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.update(999L, validUpdateReq(), adminUser.getId(), UserRole.ADMIN, httpReq));
    }

    // ── delete ───────────────────────────────────────────────────────

    @Test
    void ADMIN_프로젝트_삭제하면_배정_연쇄_삭제_후_감사_로그_기록() {
        Project project = makeProject(10L);
        ProjectAssignment pa = makeAssignment(empUser, project);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(assignmentRepository.findByProjectId(10L)).thenReturn(List.of(pa));

        service.delete(10L, adminUser.getId(), UserRole.ADMIN, httpReq);

        verify(assignmentRepository).deleteAll(List.of(pa));
        verify(projectRepository).delete(project);
        verify(auditService).log(eq(adminUser.getId()),
                eq(AuditActionType.DELETE_PROJECT),
                eq(AuditTargetType.PROJECT),
                eq(10L), anyString(), eq(httpReq));
    }

    @Test
    void ADMIN이_아니면_프로젝트_삭제_시_403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.delete(10L, salesUser.getId(), UserRole.SALES, httpReq));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(projectRepository, never()).delete(any());
    }

    @Test
    void 존재하지_않는_프로젝트_삭제_시_예외() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.delete(999L, adminUser.getId(), UserRole.ADMIN, httpReq));
    }

    // ── addMember ─────────────────────────────────────────────────────

    @Test
    void ADMIN_멤버_추가하면_배정_저장_감사_로그_기록() {
        Project project = makeProject(10L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(empUser.getId())).thenReturn(Optional.of(empUser));
        when(assignmentRepository.save(any())).thenReturn(makeAssignment(empUser, project));

        service.addMember(10L, validMemberReq(empUser.getId()), adminUser.getId(), UserRole.ADMIN, httpReq);

        verify(assignmentRepository).save(any(ProjectAssignment.class));
        verify(auditService).log(eq(adminUser.getId()),
                eq(AuditActionType.ADD_PROJECT_MEMBER),
                eq(AuditTargetType.PROJECT),
                eq(10L), anyString(), eq(httpReq));
    }

    @Test
    void ADMIN이_아니면_멤버_추가_시_403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.addMember(10L, validMemberReq(empUser.getId()),
                        salesUser.getId(), UserRole.SALES, httpReq));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void 비활성화_직원_멤버_추가_시_예외() {
        Project project = makeProject(10L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(disabledUser.getId())).thenReturn(Optional.of(disabledUser));

        assertThrows(IllegalArgumentException.class,
                () -> service.addMember(10L, validMemberReq(disabledUser.getId()),
                        adminUser.getId(), UserRole.ADMIN, httpReq));
        verify(assignmentRepository, never()).save(any());
    }

    // ── updateMember ─────────────────────────────────────────────────

    @Test
    void ADMIN_멤버_수정하면_감사_로그_기록() {
        Project project = makeProject(10L);
        ProjectAssignment pa = makeAssignment(empUser, project);
        ReflectionTestUtils.setField(pa, "id", 20L);
        when(assignmentRepository.findById(20L)).thenReturn(Optional.of(pa));

        ProjectMemberRequest req = validMemberReq(null);
        service.updateMember(10L, 20L, req, adminUser.getId(), UserRole.ADMIN, httpReq);

        verify(auditService).log(eq(adminUser.getId()),
                eq(AuditActionType.UPDATE_PROJECT_MEMBER),
                eq(AuditTargetType.PROJECT),
                eq(10L), anyString(), eq(httpReq));
    }

    @Test
    void ADMIN이_아니면_멤버_수정_시_403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateMember(10L, 20L, validMemberReq(null),
                        salesUser.getId(), UserRole.SALES, httpReq));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // ── removeMember ─────────────────────────────────────────────────

    @Test
    void ADMIN_멤버_제거하면_삭제_후_감사_로그_기록() {
        Project project = makeProject(10L);
        ProjectAssignment pa = makeAssignment(empUser, project);
        ReflectionTestUtils.setField(pa, "id", 20L);
        when(assignmentRepository.findById(20L)).thenReturn(Optional.of(pa));

        service.removeMember(10L, 20L, adminUser.getId(), UserRole.ADMIN, httpReq);

        verify(assignmentRepository).delete(pa);
        verify(auditService).log(eq(adminUser.getId()),
                eq(AuditActionType.REMOVE_PROJECT_MEMBER),
                eq(AuditTargetType.PROJECT),
                eq(10L), anyString(), eq(httpReq));
    }

    @Test
    void ADMIN이_아니면_멤버_제거_시_403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.removeMember(10L, 20L, salesUser.getId(), UserRole.SALES, httpReq));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(assignmentRepository, never()).delete(any());
    }

    // ── 헬퍼 ────────────────────────────────────────────────────────

    private User makeUser(long id, UserRole role, UserStatus status) {
        User u = User.create("user" + id + "@test.co.kr", "encoded", "사용자" + id,
                "user" + id + "@test.co.kr", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(u, "id",     id);
        ReflectionTestUtils.setField(u, "role",   role);
        ReflectionTestUtils.setField(u, "status", status);
        return u;
    }

    private Project makeProject(long id) {
        Project p = Project.create("테스트 프로젝트", "테스트 고객사", TODAY, TODAY.plusDays(30), null);
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    private ProjectAssignment makeAssignment(User user, Project project) {
        return ProjectAssignment.createForProject(project, user, "개발자", TODAY, TODAY.plusDays(30));
    }

    private ProjectCreateRequest validCreateReq(List<Long> userIds) {
        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setName("테스트 프로젝트");
        req.setStartDate(TODAY);
        req.setEndDate(TODAY.plusDays(30));
        req.setSelectedUserIds(userIds);
        return req;
    }

    private ProjectUpdateRequest validUpdateReq() {
        ProjectUpdateRequest req = new ProjectUpdateRequest();
        req.setName("수정된 프로젝트");
        req.setStartDate(TODAY);
        req.setEndDate(TODAY.plusDays(30));
        req.setStatus(ProjectStatus.ACTIVE);
        return req;
    }

    private ProjectMemberRequest validMemberReq(Long userId) {
        ProjectMemberRequest req = new ProjectMemberRequest();
        req.setUserId(userId);
        req.setStartDate(TODAY);
        req.setEndDate(TODAY.plusDays(30));
        return req;
    }
}
