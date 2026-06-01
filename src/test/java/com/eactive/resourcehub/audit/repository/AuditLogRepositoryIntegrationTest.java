package com.eactive.resourcehub.audit.repository;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditLog;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.config.JpaAuditingConfig;
import com.eactive.resourcehub.team.entity.Team;
import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AuditLogRepository PostgreSQL 통합 테스트.
 * JOIN FETCH + LEFT JOIN FETCH u.team 쿼리의 실제 DB 동작을 검증한다.
 * (LazyInitializationException 회귀 방지)
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class AuditLogRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Autowired TestEntityManager em;
    @Autowired AuditLogRepository auditLogRepository;
    @Autowired UserRepository userRepository;
    @Autowired TeamRepository teamRepository;

    private User userWithTeam;
    private User userWithoutTeam;

    @BeforeEach
    void setUp() {
        Team team = teamRepository.save(Team.create("개발팀", "개발"));

        userWithTeam = userRepository.save(User.create(
                "with-team@test.com", "encoded", "팀있음",
                "with-team@test.com", team, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0001"));

        userWithoutTeam = userRepository.save(User.create(
                "no-team@test.com", "encoded", "팀없음",
                "no-team@test.com", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0002"));

        // 각 사용자에 대해 UPLOAD 감사 로그 2건씩
        auditLogRepository.save(AuditLog.record(userWithTeam, AuditActionType.UPLOAD,
                AuditTargetType.DOCUMENT, 1L, "업로드", "127.0.0.1", null));
        auditLogRepository.save(AuditLog.record(userWithTeam, AuditActionType.UPLOAD,
                AuditTargetType.DOCUMENT, 2L, "업로드2", "127.0.0.1", null));
        auditLogRepository.save(AuditLog.record(userWithoutTeam, AuditActionType.UPLOAD,
                AuditTargetType.DOCUMENT, 3L, "업로드3", "127.0.0.1", null));
        auditLogRepository.save(AuditLog.record(userWithTeam, AuditActionType.VIEW,
                AuditTargetType.DOCUMENT_VERSION, 1L, "조회", "127.0.0.1", null));

        em.flush();
        em.clear();
    }

    // ── findByActionTypeWithUser ──────────────────────────────

    @Test
    void findByActionTypeWithUser_UPLOAD_로그_반환() {
        List<AuditLog> logs = auditLogRepository.findByActionTypeWithUser(
                AuditActionType.UPLOAD, PageRequest.of(0, 10));

        assertThat(logs).hasSize(3);
    }

    @Test
    void findByActionTypeWithUser_팀_있는_유저_team_지연로딩_없음() {
        List<AuditLog> logs = auditLogRepository.findByActionTypeWithUser(
                AuditActionType.UPLOAD, PageRequest.of(0, 10));

        // LEFT JOIN FETCH u.team 이 있으므로 team 접근 시 LazyInitializationException 없어야 함
        logs.forEach(log -> {
            String teamName = log.getUser().getTeam() != null
                    ? log.getUser().getTeam().getName() : null;
            // 팀 있는 유저는 팀 이름 조회 가능, 팀 없는 유저는 null
            assertThat(teamName == null || !teamName.isBlank()).isTrue();
        });
    }

    @Test
    void findByActionTypeWithUser_페이지_제한_적용() {
        List<AuditLog> logs = auditLogRepository.findByActionTypeWithUser(
                AuditActionType.UPLOAD, PageRequest.of(0, 2));

        assertThat(logs).hasSize(2);
    }

    @Test
    void findByActionTypeWithUser_없는_타입이면_빈_리스트() {
        List<AuditLog> logs = auditLogRepository.findByActionTypeWithUser(
                AuditActionType.DELETE, PageRequest.of(0, 10));

        assertThat(logs).isEmpty();
    }

    // ── findByActionTypeAndUserIdWithUser ─────────────────────

    @Test
    void findByActionTypeAndUserIdWithUser_특정_유저_UPLOAD_로그() {
        Page<AuditLog> page = auditLogRepository.findByActionTypeAndUserIdWithUser(
                AuditActionType.UPLOAD, userWithTeam.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        page.getContent().forEach(log ->
                assertThat(log.getUser().getId()).isEqualTo(userWithTeam.getId()));
    }

    @Test
    void findByActionTypeAndUserIdWithUser_다른_유저_로그_미포함() {
        Page<AuditLog> page = auditLogRepository.findByActionTypeAndUserIdWithUser(
                AuditActionType.UPLOAD, userWithoutTeam.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    // ── findTopTargetsByActionType ────────────────────────────

    @Test
    void findTopTargetsByActionType_UPLOAD_집계_반환() {
        List<Object[]> result = auditLogRepository.findTopTargetsByActionType(
                AuditActionType.UPLOAD, PageRequest.of(0, 5));

        assertThat(result).isNotEmpty();
        // targetId + count 쌍으로 반환됨
        result.forEach(row -> {
            assertThat(row).hasSize(2);
            assertThat((Long) row[1]).isGreaterThan(0);
        });
    }

    // ── 기본 쿼리 ─────────────────────────────────────────────

    @Test
    void findByUserId_특정_유저_로그_반환() {
        List<AuditLog> logs = auditLogRepository.findByUserId(userWithTeam.getId());
        // UPLOAD 2건 + VIEW 1건
        assertThat(logs).hasSize(3);
    }

    @Test
    void countByActionType_UPLOAD_3건() {
        assertThat(auditLogRepository.countByActionType(AuditActionType.UPLOAD)).isEqualTo(3);
    }
}
