package com.eactive.resourcehub.project.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.project.dto.ProjectAssignmentRequest;
import com.eactive.resourcehub.project.entity.AssignmentStatus;
import com.eactive.resourcehub.project.entity.ProjectAssignment;
import com.eactive.resourcehub.project.repository.ProjectAssignmentRepository;
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
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProjectAssignmentService 통합(서비스 레이어) 테스트.
 * CRUD 흐름, 권한 검증, 통계 집계, 투입 정보 선택 로직을 검증한다.
 * 스펙 ID: PA-001, PA-002, PA-004, PA-005, PA-007, PA-008.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectAssignmentServiceTest {

    @Mock ProjectAssignmentRepository assignmentRepository;
    @Mock UserRepository              userRepository;
    @Mock AuditService                auditService;
    @Mock HttpServletRequest          httpReq;

    @InjectMocks ProjectAssignmentService service;

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

    // ── 투입 등록 (create) ───────────────────────────────────────────

    @Test
    void ADMIN이_배정_등록하면_저장되고_감사_로그_기록() {
        ProjectAssignmentRequest req = validReq(empUser.getId());
        ProjectAssignment saved = makeAssignment(10L, empUser, TODAY, TODAY.plusDays(30));

        when(userRepository.findById(empUser.getId())).thenReturn(Optional.of(empUser));
        when(assignmentRepository.save(any())).thenReturn(saved);

        ProjectAssignment result = service.create(req, adminUser.getId(), UserRole.ADMIN, httpReq);

        assertNotNull(result);
        verify(assignmentRepository).save(any(ProjectAssignment.class));
        verify(auditService).log(eq(adminUser.getId()),
                eq(AuditActionType.ASSIGN_PROJECT),
                eq(AuditTargetType.PROJECT_ASSIGNMENT),
                anyLong(), anyString(), eq(httpReq));
    }

    @Test
    void ADMIN이_아니면_배정_등록_시_403() {
        ProjectAssignmentRequest req = validReq(empUser.getId());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(req, salesUser.getId(), UserRole.SALES, httpReq));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void 비활성화_직원에게_배정_등록_시_예외() {
        ProjectAssignmentRequest req = validReq(disabledUser.getId());
        when(userRepository.findById(disabledUser.getId())).thenReturn(Optional.of(disabledUser));

        assertThrows(IllegalArgumentException.class,
                () -> service.create(req, adminUser.getId(), UserRole.ADMIN, httpReq));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void 존재하지_않는_직원에게_배정_등록_시_예외() {
        ProjectAssignmentRequest req = validReq(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.create(req, adminUser.getId(), UserRole.ADMIN, httpReq));
    }

    // ── 투입 조회 ────────────────────────────────────────────────────

    @Test
    void 직원_투입이력_전체_조회() {
        ProjectAssignment pa = makeAssignment(10L, empUser, TODAY.minusDays(30), TODAY);
        when(assignmentRepository.findByUserId(empUser.getId())).thenReturn(List.of(pa));

        List<ProjectAssignment> result = service.findByUserId(empUser.getId());

        assertEquals(1, result.size());
        assertSame(pa, result.get(0));
    }

    @Test
    void 월간_투입목록_필터없이_조회() {
        YearMonth ym = YearMonth.now();
        ProjectAssignment pa = makeAssignment(10L, empUser, ym.atDay(1), ym.atEndOfMonth());
        when(assignmentRepository.findForMonth(any(), any())).thenReturn(List.of(pa));

        List<ProjectAssignment> result = service.getMonthlyAssignments(ym, null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void 월간_투입목록_직원명_필터_적용() {
        YearMonth ym = YearMonth.now();
        ProjectAssignment pa = makeAssignment(10L, empUser, ym.atDay(1), ym.atEndOfMonth());
        when(assignmentRepository.findForMonth(any(), any())).thenReturn(List.of(pa));

        // empUser.getName() = "사용자3", "사용자2"는 매칭 안 됨
        List<ProjectAssignment> matched    = service.getMonthlyAssignments(ym, "사용자3", null, null);
        List<ProjectAssignment> notMatched = service.getMonthlyAssignments(ym, "없는이름", null, null);

        assertEquals(1, matched.size());
        assertTrue(notMatched.isEmpty());
    }

    @Test
    void 종료_임박_배정_조회_14일_기준() {
        when(assignmentRepository.findEndingSoon(any(), any())).thenReturn(List.of());

        service.findEndingSoon(14);

        verify(assignmentRepository).findEndingSoon(eq(TODAY), eq(TODAY.plusDays(14)));
    }

    // ── 투입 수정 (update) ───────────────────────────────────────────

    @Test
    void ADMIN이_배정_수정하면_감사_로그_기록() {
        ProjectAssignment pa = makeAssignment(10L, empUser, TODAY, TODAY.plusDays(30));
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(pa));

        ProjectAssignmentRequest req = validReq(empUser.getId());
        service.update(10L, req, adminUser.getId(), UserRole.ADMIN, httpReq);

        verify(auditService).log(eq(adminUser.getId()),
                eq(AuditActionType.UPDATE_ASSIGNMENT),
                eq(AuditTargetType.PROJECT_ASSIGNMENT),
                eq(10L), anyString(), eq(httpReq));
    }

    @Test
    void ADMIN이_아니면_배정_수정_시_403() {
        ProjectAssignmentRequest req = validReq(empUser.getId());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.update(10L, req, salesUser.getId(), UserRole.SALES, httpReq));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void 존재하지_않는_배정_수정_시_예외() {
        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());
        ProjectAssignmentRequest req = validReq(empUser.getId());

        assertThrows(IllegalArgumentException.class,
                () -> service.update(999L, req, adminUser.getId(), UserRole.ADMIN, httpReq));
    }

    // ── 투입 삭제 (delete) ───────────────────────────────────────────

    @Test
    void ADMIN이_배정_삭제하면_감사_로그_기록() {
        ProjectAssignment pa = makeAssignment(10L, empUser, TODAY, TODAY.plusDays(30));
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(pa));

        service.delete(10L, adminUser.getId(), UserRole.ADMIN, httpReq);

        verify(assignmentRepository).delete(pa);
        verify(auditService).log(eq(adminUser.getId()),
                eq(AuditActionType.DELETE_ASSIGNMENT),
                eq(AuditTargetType.PROJECT_ASSIGNMENT),
                eq(10L), anyString(), eq(httpReq));
    }

    @Test
    void ADMIN이_아니면_배정_삭제_시_403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.delete(10L, salesUser.getId(), UserRole.SALES, httpReq));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(assignmentRepository, never()).delete(any());
    }

    @Test
    void 존재하지_않는_배정_삭제_시_예외() {
        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.delete(999L, adminUser.getId(), UserRole.ADMIN, httpReq));
    }

    // ── 대시보드 통계 (getDeployStats) ──────────────────────────────

    @Test
    void 통계_ADMIN_직원은_미투입_계산에서_제외() {
        // ADMIN 1명 + 비ADMIN 2명 (empUser, salesUser)
        when(userRepository.findByStatus(UserStatus.ACTIVE))
                .thenReturn(List.of(adminUser, empUser, salesUser));
        when(assignmentRepository.findActiveOn(any())).thenReturn(List.of());
        when(assignmentRepository.findStartingBetween(any(), any())).thenReturn(List.of());
        when(assignmentRepository.findEndingBetween(any(), any())).thenReturn(List.of());

        DeployStats stats = service.getDeployStats();

        assertEquals(0, stats.currentlyDeployed());
        assertEquals(2, stats.notDeployed(), "ADMIN 제외 → 미투입 2명");
    }

    @Test
    void 통계_미투입은_음수가_되지_않음() {
        // 비ADMIN 2명, 투입 중 3명(다른 직원이 여러 프로젝트) — 음수 방지
        ProjectAssignment pa1 = makeAssignment(10L, empUser,   TODAY.minusDays(1), TODAY.plusDays(10));
        ProjectAssignment pa2 = makeAssignment(20L, salesUser, TODAY.minusDays(1), TODAY.plusDays(10));
        ProjectAssignment pa3 = makeAssignment(30L, empUser,   TODAY.minusDays(2), TODAY.plusDays(5));

        when(userRepository.findByStatus(UserStatus.ACTIVE)).thenReturn(List.of(empUser, salesUser));
        when(assignmentRepository.findActiveOn(any())).thenReturn(List.of(pa1, pa2, pa3));
        when(assignmentRepository.findStartingBetween(any(), any())).thenReturn(List.of());
        when(assignmentRepository.findEndingBetween(any(), any())).thenReturn(List.of());

        DeployStats stats = service.getDeployStats();

        assertTrue(stats.notDeployed() >= 0, "미투입 수는 0 이상이어야 합니다");
    }

    @Test
    void 통계_현재_투입_중_직원_수는_distinct_처리() {
        // empUser 2개 배정 → 1명으로 카운트
        ProjectAssignment pa1 = makeAssignment(10L, empUser, TODAY.minusDays(1), TODAY.plusDays(5));
        ProjectAssignment pa2 = makeAssignment(20L, empUser, TODAY.minusDays(2), TODAY.plusDays(10));

        when(userRepository.findByStatus(UserStatus.ACTIVE)).thenReturn(List.of(empUser));
        when(assignmentRepository.findActiveOn(any())).thenReturn(List.of(pa1, pa2));
        when(assignmentRepository.findStartingBetween(any(), any())).thenReturn(List.of());
        when(assignmentRepository.findEndingBetween(any(), any())).thenReturn(List.of());

        DeployStats stats = service.getDeployStats();

        assertEquals(1, stats.currentlyDeployed(), "같은 직원 복수 배정은 1명으로 카운트");
    }

    // ── 투입 정보 선택 로직 ──────────────────────────────────────────

    @Test
    void 현재_투입이_있으면_현재_배정_우선_선택() {
        ProjectAssignment pa1 = makeAssignment(10L, empUser, TODAY.minusDays(10), TODAY.plusDays(5));
        ProjectAssignment pa2 = makeAssignment(20L, empUser, TODAY.minusDays(5),  TODAY.plusDays(10));

        when(assignmentRepository.findActiveOn(any())).thenReturn(List.of(pa1, pa2));

        Map<Long, ProjectAssignment> result = service.getActiveAssignmentsByUserId();

        assertEquals(1, result.size(), "동일 직원은 하나의 엔트리만");
        assertSame(pa1, result.get(empUser.getId()), "먼저 반환된 pa1이 선택");
    }

    @Test
    void 현재_투입이_없고_예정이_있으면_가장_빠른_예정_선택() {
        // 정렬: startDate ASC → near이 먼저
        ProjectAssignment near = makeAssignment(10L, empUser, TODAY.plusDays(5),  TODAY.plusDays(30));
        ProjectAssignment far  = makeAssignment(20L, empUser, TODAY.plusDays(15), TODAY.plusDays(45));

        when(assignmentRepository.findPlannedFrom(any())).thenReturn(List.of(near, far));

        Map<Long, ProjectAssignment> result = service.getNextAssignmentsByUserId();

        assertEquals(1, result.size());
        assertSame(near, result.get(empUser.getId()), "더 빠른 시작일(near)이 선택");
    }

    @Test
    void 현재_투입도_예정도_없으면_빈_맵_반환() {
        when(assignmentRepository.findActiveOn(any())).thenReturn(List.of());
        when(assignmentRepository.findPlannedFrom(any())).thenReturn(List.of());

        Map<Long, ProjectAssignment> currentMap = service.getActiveAssignmentsByUserId();
        Map<Long, ProjectAssignment> nextMap    = service.getNextAssignmentsByUserId();

        assertTrue(currentMap.isEmpty(), "현재 투입 없으면 빈 맵");
        assertTrue(nextMap.isEmpty(),    "예정 투입 없으면 빈 맵");
    }

    @Test
    void 여러_직원의_배정이_각_직원별로_한_건씩_선택됨() {
        ProjectAssignment paEmp   = makeAssignment(10L, empUser,   TODAY.minusDays(1), TODAY.plusDays(10));
        ProjectAssignment paSales = makeAssignment(20L, salesUser, TODAY.minusDays(2), TODAY.plusDays(5));

        when(assignmentRepository.findActiveOn(any())).thenReturn(List.of(paEmp, paSales));

        Map<Long, ProjectAssignment> result = service.getActiveAssignmentsByUserId();

        assertEquals(2, result.size());
        assertSame(paEmp,   result.get(empUser.getId()));
        assertSame(paSales, result.get(salesUser.getId()));
    }

    // ── 중복 경고 ────────────────────────────────────────────────────

    @Test
    void 겹치는_배정_확인_요청이_repository로_위임됨() {
        when(assignmentRepository.findOverlapping(any(), any(), any(), any())).thenReturn(List.of());

        List<ProjectAssignment> result = service.checkOverlap(
                empUser.getId(), TODAY, TODAY.plusDays(30), null);

        assertTrue(result.isEmpty());
        verify(assignmentRepository).findOverlapping(
                eq(empUser.getId()), eq(TODAY), eq(TODAY.plusDays(30)), isNull());
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

    private ProjectAssignment makeAssignment(long id, User user, LocalDate start, LocalDate end) {
        ProjectAssignment pa = ProjectAssignment.create(
                user, "테스트 프로젝트", "테스트 고객사", "개발자", start, end, null);
        ReflectionTestUtils.setField(pa, "id", id);
        return pa;
    }

    private ProjectAssignmentRequest validReq(Long userId) {
        ProjectAssignmentRequest req = new ProjectAssignmentRequest();
        req.setUserId(userId);
        req.setProjectName("테스트 프로젝트");
        req.setClientName("테스트 고객사");
        req.setStartDate(TODAY);
        req.setEndDate(TODAY.plusDays(30));
        return req;
    }
}
