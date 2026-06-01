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
import java.time.temporal.ChronoUnit;
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

    /** 대시보드 개인 현황: 해당 사용자의 ACTIVE + PLANNED 배정, 시작일 오름차순 */
    @Transactional(readOnly = true)
    public List<ProjectAssignment> findActiveAndPlannedForUser(Long userId) {
        return assignmentRepository.findByUserId(userId).stream()
                .filter(pa -> pa.getStatus() == AssignmentStatus.ACTIVE
                           || pa.getStatus() == AssignmentStatus.PLANNED)
                .sorted(java.util.Comparator.comparing(ProjectAssignment::getStartDate))
                .toList();
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

    /** 대시보드 통계 — COUNT 쿼리로 목록 전체 로드를 피함 */
    @Transactional(readOnly = true)
    public DeployStats getDeployStats() {
        LocalDate today  = LocalDate.now();
        YearMonth ym     = YearMonth.from(today);
        LocalDate mStart = ym.atDay(1);
        LocalDate mEnd   = ym.atEndOfMonth();

        long startingThisMonth = assignmentRepository.countStartingBetween(mStart, mEnd);
        long endingThisMonth   = assignmentRepository.countEndingBetween(mStart, mEnd);
        long currentlyDeployed = assignmentRepository.countActiveDistinctUsersOn(today);
        long totalNonAdmin     = userRepository.countByStatusAndRoleNot(UserStatus.ACTIVE, UserRole.ADMIN);
        long notDeployed       = Math.max(0, totalNonAdmin - currentlyDeployed);

        return new DeployStats(startingThisMonth, endingThisMonth, currentlyDeployed, notDeployed);
    }

    /** 대시보드 투입 현황: ACTIVE·PLANNED 배정을 프로젝트별로 그룹화 */
    @Transactional(readOnly = true)
    public List<ProjectGroupSummary> getProjectGroupSummaries() {
        LocalDate today = LocalDate.now();
        List<ProjectAssignment> all = assignmentRepository.findActiveAndPlanned();

        Map<Long, List<ProjectAssignment>> byProject = all.stream()
            .collect(Collectors.groupingBy(pa -> pa.getProject().getId()));

        return byProject.values().stream()
            .map(list -> {
                ProjectAssignment first = list.get(0);
                AssignmentStatus dominant = list.stream()
                    .anyMatch(pa -> pa.getStatus() == AssignmentStatus.ACTIVE)
                    ? AssignmentStatus.ACTIVE : AssignmentStatus.PLANNED;

                List<String> allNames = list.stream()
                    .map(ProjectAssignment::getDisplayName)
                    .distinct()
                    .sorted()
                    .toList();
                List<String> shown = allNames.stream().limit(10).toList();

                LocalDate latestEnd = list.stream()
                    .map(ProjectAssignment::getEndDate)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
                boolean endingSoon = dominant == AssignmentStatus.ACTIVE
                    && latestEnd != null && !latestEnd.isAfter(today.plusDays(14));

                return new ProjectGroupSummary(
                    first.getProject().getName(), dominant, latestEnd,
                    shown, allNames.size(), endingSoon);
            })
            .sorted(Comparator
                .comparing((ProjectGroupSummary g) -> g.status() == AssignmentStatus.ACTIVE ? 0 : 1)
                .thenComparing(g -> g.endDate() != null ? g.endDate() : LocalDate.MAX))
            .toList();
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

    /** 프로젝트별 배정 인원 수 맵 (취소 제외) */
    @Transactional(readOnly = true)
    public Map<Long, Long> getMemberCountsByProject() {
        return assignmentRepository.countAssignmentsByProject().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]));
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

    // ── DTO ───────────────────────────────────────────────────────

    /** 대시보드 투입 프로젝트 현황 — 프로젝트 단위 요약 */
    public record ProjectGroupSummary(
        String projectName,
        AssignmentStatus status,
        LocalDate endDate,
        List<String> memberNames,
        int totalCount,
        boolean endingSoon
    ) {
        public String memberSummary() {
            if (memberNames.isEmpty()) return "-";
            String head = String.join(", ", memberNames);
            int extra = totalCount - memberNames.size();
            return extra > 0 ? head + " 외 " + extra + "명" : head;
        }
        public long remainingDays() {
            if (endDate == null) return 0;
            return Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), endDate));
        }
    }
}
