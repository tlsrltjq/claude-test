package com.eactive.resourcehub.project.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.project.dto.ProjectCreateRequest;
import com.eactive.resourcehub.project.dto.ProjectMemberRequest;
import com.eactive.resourcehub.project.dto.ProjectUpdateRequest;
import com.eactive.resourcehub.project.entity.Project;
import com.eactive.resourcehub.project.entity.ProjectAssignment;
import com.eactive.resourcehub.project.entity.ProjectStatus;
import com.eactive.resourcehub.project.repository.ProjectAssignmentRepository;
import com.eactive.resourcehub.project.repository.ProjectRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    // ── 조회 ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
    }

    @Transactional(readOnly = true)
    public List<Project> getMonthlyProjects(YearMonth ym) {
        return projectRepository.findForMonth(ym.atDay(1), ym.atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public List<Project> getAllNonCancelledProjects() {
        return projectRepository.findAllNonCancelled();
    }

    @Transactional(readOnly = true)
    public List<ProjectAssignment> getMembersForProject(Long projectId) {
        return assignmentRepository.findByProjectId(projectId);
    }

    /** 프로젝트 멤버 추가 모달용 활성 직원 목록 (ADMIN 제외, 인력표 노출 팀만, 이름순) */
    @Transactional(readOnly = true)
    public List<User> findAssignableUsers() {
        return userRepository.findByStatusWithTeam(UserStatus.ACTIVE).stream()
                .filter(u -> u.getRole() != UserRole.ADMIN)
                .filter(u -> u.getTeam() != null && u.getTeam().isProjectTeam())
                .sorted(Comparator.comparing(User::getName))
                .collect(java.util.stream.Collectors.toList());
    }

    // ── 프로젝트 CRUD (ADMIN 전용) ────────────────────────────────

    @Transactional
    public Project create(ProjectCreateRequest req, Long actorId,
                          UserRole actorRole, HttpServletRequest httpReq) {
        requireAdmin(actorRole);
        req.validate();

        Project project = Project.create(req.getName(), req.getClientName(),
                req.getStartDate(), req.getEndDate(), req.getMemo());
        Project saved = projectRepository.save(project);

        if (req.getSelectedUserIds() != null) {
            for (Long userId : req.getSelectedUserIds()) {
                addMemberInternal(saved, userId, null,
                        saved.getStartDate(), saved.getEndDate());
            }
        }

        log.info("프로젝트 생성 — id={}, name={}", saved.getId(), saved.getName());
        auditService.log(actorId, AuditActionType.CREATE_PROJECT,
                AuditTargetType.PROJECT, saved.getId(), saved.getName(), httpReq);
        return saved;
    }

    @Transactional
    public Project update(Long id, ProjectUpdateRequest req, Long actorId,
                          UserRole actorRole, HttpServletRequest httpReq) {
        requireAdmin(actorRole);
        req.validate();

        Project project = findById(id);
        ProjectStatus newStatus = req.getStatus() != null ? req.getStatus() : project.getStatus();
        project.update(req.getName(), req.getClientName(),
                req.getStartDate(), req.getEndDate(),
                req.getMemo(), newStatus);

        if (newStatus == ProjectStatus.CANCELLED) {
            assignmentRepository.cancelByProject(id);
        }

        log.info("프로젝트 수정 — id={}, name={}", id, project.getName());
        auditService.log(actorId, AuditActionType.UPDATE_PROJECT,
                AuditTargetType.PROJECT, id, project.getName(), httpReq);
        return project;
    }

    @Transactional
    public void delete(Long id, Long actorId, UserRole actorRole, HttpServletRequest httpReq) {
        requireAdmin(actorRole);
        Project project = findById(id);
        String name = project.getName();

        List<ProjectAssignment> members = assignmentRepository.findByProjectId(id);
        assignmentRepository.deleteAll(members);
        projectRepository.delete(project);

        log.info("프로젝트 삭제 — id={}, name={}", id, name);
        auditService.log(actorId, AuditActionType.DELETE_PROJECT,
                AuditTargetType.PROJECT, id, name, httpReq);
    }

    // ── 멤버 관리 (ADMIN 전용) ────────────────────────────────────

    @Transactional
    public ProjectAssignment addMember(Long projectId, ProjectMemberRequest req,
                                       Long actorId, UserRole actorRole,
                                       HttpServletRequest httpReq) {
        requireAdmin(actorRole);
        req.validateForAdd();

        Project project = findById(projectId);
        ProjectAssignment pa = addMemberInternal(project, req.getUserId(),
                req.getRole(), req.getStartDate(), req.getEndDate());

        log.info("프로젝트 멤버 추가 — projectId={}, userId={}", projectId, req.getUserId());
        auditService.log(actorId, AuditActionType.ADD_PROJECT_MEMBER,
                AuditTargetType.PROJECT, projectId,
                pa.getUser().getName() + " → " + project.getName(), httpReq);
        return pa;
    }

    @Transactional
    public ProjectAssignment updateMember(Long projectId, Long assignmentId,
                                          ProjectMemberRequest req,
                                          Long actorId, UserRole actorRole,
                                          HttpServletRequest httpReq) {
        requireAdmin(actorRole);
        req.validateForUpdate();

        ProjectAssignment pa = findAssignment(assignmentId);
        pa.updateMember(req.getRole(), req.getStartDate(), req.getEndDate(), req.getStatus());

        log.info("프로젝트 멤버 수정 — assignmentId={}", assignmentId);
        auditService.log(actorId, AuditActionType.UPDATE_PROJECT_MEMBER,
                AuditTargetType.PROJECT, projectId,
                pa.getUser().getName() + " 투입 기간 수정", httpReq);
        return pa;
    }

    @Transactional
    public void removeMember(Long projectId, Long assignmentId,
                             Long actorId, UserRole actorRole,
                             HttpServletRequest httpReq) {
        requireAdmin(actorRole);
        ProjectAssignment pa = findAssignment(assignmentId);
        String desc = pa.getUser().getName() + " → " + pa.getProject().getName();
        assignmentRepository.delete(pa);

        log.info("프로젝트 멤버 제거 — assignmentId={}", assignmentId);
        auditService.log(actorId, AuditActionType.REMOVE_PROJECT_MEMBER,
                AuditTargetType.PROJECT, projectId, desc, httpReq);
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────────

    private ProjectAssignment addMemberInternal(Project project, Long userId,
                                                 String role,
                                                 LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직원입니다."));
        if (user.getStatus() == UserStatus.DISABLED)
            throw new IllegalArgumentException("비활성화된 직원에게는 배정을 생성할 수 없습니다.");

        ProjectAssignment pa = ProjectAssignment.createForProject(
                project, user, role, startDate, endDate);
        return assignmentRepository.save(pa);
    }

    private ProjectAssignment findAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 배정입니다."));
    }

    private void requireAdmin(UserRole role) {
        if (role != UserRole.ADMIN && role != UserRole.SALES)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 또는 영업 담당자만 프로젝트를 관리할 수 있습니다.");
    }
}
