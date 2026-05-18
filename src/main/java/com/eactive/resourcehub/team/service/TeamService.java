package com.eactive.resourcehub.team.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.team.dto.TeamRequest;
import com.eactive.resourcehub.team.entity.Team;
import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Team> findProjectTeams() {
        return teamRepository.findByProjectTeamTrue();
    }

    @Transactional
    public boolean toggleProjectTeam(Long id) {
        Team team = findById(id);
        team.toggleProjectTeam();
        log.info("팀 인력표 노출 토글 — teamId={}, name={}, projectTeam={}", id, team.getName(), team.isProjectTeam());
        return team.isProjectTeam();
    }

    @Transactional(readOnly = true)
    public long count() {
        return teamRepository.count();
    }

    @Transactional(readOnly = true)
    public Team findById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
    }

    @Transactional
    public Team create(TeamRequest request, Long actorUserId, HttpServletRequest httpRequest) {
        if (teamRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 사용 중인 팀 이름입니다: " + request.getName());
        }
        Team team = Team.create(request.getName(), request.getDescription());
        Team saved = teamRepository.save(team);
        log.info("팀 생성 — teamId={}, name={}", saved.getId(), saved.getName());
        auditService.log(actorUserId, AuditActionType.CREATE,
                AuditTargetType.TEAM, saved.getId(), "팀 생성: " + saved.getName(), httpRequest);
        return saved;
    }

    @Transactional
    public Team update(Long id, TeamRequest request, Long actorUserId, HttpServletRequest httpRequest) {
        Team team = findById(id);
        if (!team.getName().equals(request.getName()) && teamRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 사용 중인 팀 이름입니다: " + request.getName());
        }
        team.update(request.getName(), request.getDescription());
        log.info("팀 수정 — teamId={}, name={}", id, request.getName());
        auditService.log(actorUserId, AuditActionType.UPDATE,
                AuditTargetType.TEAM, id, "팀 수정: " + request.getName(), httpRequest);
        return team;
    }

    @Transactional
    public void delete(Long id, Long actorUserId, HttpServletRequest httpRequest) {
        Team team = findById(id);
        long memberCount = userRepository.findByTeamId(id).size();
        if (memberCount > 0) {
            throw new IllegalArgumentException(
                    "소속 직원이 있는 팀은 삭제할 수 없습니다. (소속 인원: " + memberCount + "명)");
        }
        teamRepository.delete(team);
        log.info("팀 삭제 — teamId={}, name={}", id, team.getName());
        auditService.log(actorUserId, AuditActionType.DELETE,
                AuditTargetType.TEAM, id, "팀 삭제: " + team.getName(), httpRequest);
    }
}
