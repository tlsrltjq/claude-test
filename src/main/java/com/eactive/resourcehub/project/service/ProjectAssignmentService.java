package com.eactive.resourcehub.project.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.project.entity.AssignmentStatus;
import com.eactive.resourcehub.project.entity.ProjectAssignment;
import com.eactive.resourcehub.project.repository.ProjectAssignmentRepository;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.user.entity.UserStatus;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectAssignmentService {

    private final ProjectAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    // ── 조회 ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProjectAssignment> getMonthlyAssignments(YearMonth ym,
                                                          String qEmployee,
                                                          String qProject,
                                                          AssignmentStatus statusFilter) {
        List<ProjectAssignment> all =
                assignmentRepository.findForMonth(ym.atDay(1), ym.atEndOfMonth());
        return all.stream()
                .filter(pa -> pa.getUser().getTeam() != null && pa.getUser().getTeam().isProjectTeam())
                .filter(pa -> matchesFilter(pa, qEmployee, qProject, statusFilter))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectAssignment> findByUserId(Long userId) {
        return assignmentRepository.findByUserId(userId);
    }

    /** 인력표용: userId → 오늘 기준 첫 번째 ACTIVE 배정 (없으면 null) */
    @Transactional(readOnly = true)
    public Map<Long, ProjectAssignment> getActiveAssignmentsByUserId() {
        return assignmentRepository.findActiveOn(LocalDate.now()).stream()
                .collect(Collectors.toMap(
                        pa -> pa.getUser().getId(),
                        pa -> pa,
                        (a, b) -> a));
    }

    /** 인력표용: userId → 가장 가까운 다음 PLANNED 배정 (없으면 null) */
    @Transactional(readOnly = true)
    public Map<Long, ProjectAssignment> getNextAssignmentsByUserId() {
        return assignmentRepository.findPlannedFrom(LocalDate.now()).stream()
                .collect(Collectors.toMap(
                        pa -> pa.getUser().getId(),
                        pa -> pa,
                        (a, b) -> a));
    }

    /** 대시보드 통계 */
    @Transactional(readOnly = true)
    public DeployStats getDeployStats() {
        LocalDate today  = LocalDate.now();
        YearMonth ym     = YearMonth.from(today);
        LocalDate mStart = ym.atDay(1);
        LocalDate mEnd   = ym.atEndOfMonth();

        long startingThisMonth = assignmentRepository.findStartingBetween(mStart, mEnd).size();
        long endingThisMonth   = assignmentRepository.findEndingBetween(mStart, mEnd).size();
        long currentlyDeployed = assignmentRepository.findActiveOn(today).stream()
                .map(pa -> pa.getUser().getId()).distinct().count();
        long totalNonAdmin     = userRepository.findByStatus(UserStatus.ACTIVE).stream()
                .filter(u -> u.getRole() != UserRole.ADMIN).count();
        long notDeployed       = Math.max(0, totalNonAdmin - currentlyDeployed);

        return new DeployStats(startingThisMonth, endingThisMonth, currentlyDeployed, notDeployed);
    }

    /** 종료 임박 배정 목록 (대시보드 경고용) */
    @Transactional(readOnly = true)
    public List<ProjectAssignment> findEndingSoon(int withinDays) {
        LocalDate today = LocalDate.now();
        return assignmentRepository.findEndingSoon(today, today.plusDays(withinDays));
    }

    /** 중복 경고 조회 — 저장을 막지는 않음 */
    @Transactional(readOnly = true)
    public List<ProjectAssignment> checkOverlap(Long userId, LocalDate start,
                                                 LocalDate end, Long excludeId) {
        return assignmentRepository.findOverlapping(userId, start, end, excludeId);
    }

    /** 인력표·캘린더 모달용 활성 직원 목록 (인력표 노출 팀만) */
    @Transactional(readOnly = true)
    public List<User> findAssignableUsers() {
        return userRepository.findByStatusWithTeam(UserStatus.ACTIVE).stream()
                .filter(u -> u.getRole() != UserRole.ADMIN)
                .filter(u -> u.getTeam() != null && u.getTeam().isProjectTeam())
                .sorted(Comparator.comparing(User::getName))
                .collect(Collectors.toList());
    }

    // ── 삭제 (ADMIN 전용) ─────────────────────────────────────────

    @Transactional
    public void delete(Long id, Long actorId, UserRole actorRole, HttpServletRequest httpReq) {
        requireAdmin(actorRole);
        ProjectAssignment pa = findById(id);
        String desc = pa.getUser().getName() + " → " + pa.getProject().getName();
        assignmentRepository.delete(pa);

        log.info("프로젝트 배정 삭제 — id={}", id);
        auditService.log(actorId, AuditActionType.DELETE_ASSIGNMENT,
                AuditTargetType.PROJECT_ASSIGNMENT, id, "배정 삭제: " + desc, httpReq);
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────────

    private ProjectAssignment findById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 배정입니다."));
    }

    private void requireAdmin(UserRole role) {
        if (role != UserRole.ADMIN && role != UserRole.SALES)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 또는 영업 담당자만 배정을 관리할 수 있습니다.");
    }

    private boolean matchesFilter(ProjectAssignment pa, String qEmployee,
                                   String qProject, AssignmentStatus statusFilter) {
        if (qEmployee != null && !qEmployee.isBlank()) {
            String name = pa.getUser().getName() != null ? pa.getUser().getName() : "";
            if (!name.toLowerCase().contains(qEmployee.toLowerCase())) return false;
        }
        if (qProject != null && !qProject.isBlank()) {
            String projectName = pa.getProject().getName();
            if (!projectName.toLowerCase().contains(qProject.toLowerCase())) return false;
        }
        if (statusFilter != null && pa.getStatus() != statusFilter) return false;
        return true;
    }
}
