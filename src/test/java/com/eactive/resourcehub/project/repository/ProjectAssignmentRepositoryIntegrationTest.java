package com.eactive.resourcehub.project.repository;

import com.eactive.resourcehub.common.config.JpaAuditingConfig;
import com.eactive.resourcehub.project.entity.AssignmentStatus;
import com.eactive.resourcehub.project.entity.Project;
import com.eactive.resourcehub.project.entity.ProjectAssignment;
import com.eactive.resourcehub.project.entity.ProjectStatus;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class ProjectAssignmentRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Autowired TestEntityManager em;
    @Autowired ProjectAssignmentRepository assignmentRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired UserRepository userRepository;

    private static final LocalDate TODAY = LocalDate.now();

    private Project project;
    private User userA;
    private User userB;
    private ProjectAssignment activeAssignment;
    private ProjectAssignment plannedAssignment;
    private ProjectAssignment cancelledAssignment;

    @BeforeEach
    void setUp() {
        project = projectRepository.save(
                Project.create("테스트 프로젝트", "고객A",
                        TODAY.minusDays(30), TODAY.plusDays(30), null));

        userA = User.create("usera@test.com", "encoded", "직원A",
                "usera@test.com", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-1111-1111");
        userA.activate();
        userRepository.save(userA);

        userB = User.create("userb@test.com", "encoded", "직원B",
                "userb@test.com", null, Position.STAFF,
                LocalDate.of(1992, 2, 2), "010-2222-2222");
        userB.activate();
        userRepository.save(userB);

        // ACTIVE 배정 (현재 진행 중)
        activeAssignment = assignmentRepository.save(
                ProjectAssignment.createForProject(project, userA, "개발",
                        TODAY.minusDays(10), TODAY.plusDays(20)));

        // PLANNED 배정 (미래 시작)
        plannedAssignment = ProjectAssignment.createForProject(project, userB, "QA",
                TODAY.plusDays(5), TODAY.plusDays(30));
        assignmentRepository.save(plannedAssignment);

        // CANCELLED 배정
        ProjectAssignment cancelled = ProjectAssignment.createForProject(project, userA, "기획",
                TODAY.minusDays(5), TODAY.plusDays(5));
        ReflectionTestUtils.setField(cancelled, "status", AssignmentStatus.CANCELLED);
        cancelledAssignment = assignmentRepository.save(cancelled);

        em.flush();
        em.clear();
    }

    // ── findByProjectId ───────────────────────────────────────

    @Test
    void findByProjectId_CANCELLED_제외_반환() {
        List<ProjectAssignment> result = assignmentRepository.findByProjectId(project.getId());

        // ACTIVE + PLANNED = 2건, CANCELLED 제외
        assertThat(result).hasSize(2);
        result.forEach(pa -> assertThat(pa.getStatus()).isNotEqualTo(AssignmentStatus.CANCELLED));
    }

    @Test
    void findByProjectId_유저_팀_페치조인() {
        List<ProjectAssignment> result = assignmentRepository.findByProjectId(project.getId());

        // JOIN FETCH pa.user → user 접근 시 LazyInit 없음
        result.forEach(pa -> assertThat(pa.getUser().getName()).isNotBlank());
    }

    // ── findByUserId ──────────────────────────────────────────

    @Test
    void findByUserId_특정_유저_배정_이력_반환() {
        List<ProjectAssignment> result = assignmentRepository.findByUserId(userA.getId());

        // userA: ACTIVE + CANCELLED = 2건
        assertThat(result).hasSize(2);
        result.forEach(pa -> assertThat(pa.getUser().getId()).isEqualTo(userA.getId()));
    }

    // ── findForMonth ──────────────────────────────────────────

    @Test
    void findForMonth_이번달에_겹치는_배정_반환() {
        LocalDate monthStart = TODAY.withDayOfMonth(1);
        LocalDate monthEnd   = TODAY.withDayOfMonth(TODAY.lengthOfMonth());

        List<ProjectAssignment> result = assignmentRepository.findForMonth(monthStart, monthEnd);

        // 모든 배정(CANCELLED 포함)이 이번 달과 겹침
        assertThat(result).hasSize(3);
    }

    // ── findActiveOn ──────────────────────────────────────────

    @Test
    void findActiveOn_오늘_기준_ACTIVE_배정_반환() {
        List<ProjectAssignment> result = assignmentRepository.findActiveOn(TODAY);

        assertThat(result).anyMatch(pa -> pa.getId().equals(activeAssignment.getId()));
        assertThat(result).noneMatch(pa -> pa.getId().equals(cancelledAssignment.getId()));
    }

    // ── findOverlapping ───────────────────────────────────────

    @Test
    void findOverlapping_겹치는_기간_감지() {
        // userA의 activeAssignment(TODAY-10 ~ TODAY+20)와 겹치는 기간 조회
        List<ProjectAssignment> overlapping = assignmentRepository.findOverlapping(
                userA.getId(),
                TODAY.minusDays(5), TODAY.plusDays(5),
                null);

        // ACTIVE 배정이 감지됨 (CANCELLED는 제외됨)
        assertThat(overlapping).anyMatch(pa -> pa.getId().equals(activeAssignment.getId()));
    }

    @Test
    void findOverlapping_자신_ID_제외() {
        List<ProjectAssignment> overlapping = assignmentRepository.findOverlapping(
                userA.getId(),
                TODAY.minusDays(5), TODAY.plusDays(5),
                activeAssignment.getId()); // 자기 자신 제외

        assertThat(overlapping).noneMatch(pa -> pa.getId().equals(activeAssignment.getId()));
    }

    @Test
    void findOverlapping_겹치지_않는_기간은_빈_리스트() {
        List<ProjectAssignment> overlapping = assignmentRepository.findOverlapping(
                userA.getId(),
                TODAY.plusDays(50), TODAY.plusDays(80),
                null);

        // userA의 배정(TODAY+20 까지)과 겹치지 않음
        assertThat(overlapping).isEmpty();
    }

    // ── findEndingSoon ────────────────────────────────────────

    @Test
    void findEndingSoon_30일_이내_종료_ACTIVE_배정() {
        List<ProjectAssignment> result = assignmentRepository.findEndingSoon(
                TODAY, TODAY.plusDays(30));

        // activeAssignment.endDate = TODAY+20 → 포함
        assertThat(result).anyMatch(pa -> pa.getId().equals(activeAssignment.getId()));
    }

    // ── @Modifying cancelByProject ────────────────────────────

    @Test
    void cancelByProject_프로젝트_전체_배정_CANCELLED_처리() {
        int updated = assignmentRepository.cancelByProject(project.getId());

        // ACTIVE + PLANNED 2건이 CANCELLED로 변경됨
        assertThat(updated).isEqualTo(2);

        em.clear();
        List<ProjectAssignment> remaining = assignmentRepository.findByProjectId(project.getId());
        assertThat(remaining).isEmpty(); // CANCELLED 제외하면 0건
    }

    // ── @Modifying 상태 자동 전환 ─────────────────────────────

    @Test
    void expireActive_endDate_경과한_ACTIVE_ENDED로_전환() {
        // endDate가 어제인 ACTIVE 배정 추가
        ProjectAssignment willExpire = ProjectAssignment.createForProject(
                project, userB, "설계",
                TODAY.minusDays(20), TODAY.minusDays(1));
        assignmentRepository.save(willExpire);
        em.flush();
        em.clear();

        int updated = assignmentRepository.expireActive(TODAY);

        assertThat(updated).isGreaterThanOrEqualTo(1);
        em.clear();
        ProjectAssignment expired = assignmentRepository.findById(willExpire.getId()).orElseThrow();
        assertThat(expired.getStatus()).isEqualTo(AssignmentStatus.ENDED);
    }

    @Test
    void activatePlanned_startDate_도달한_PLANNED_ACTIVE로_전환() {
        // startDate가 어제, 아직 endDate 전인 PLANNED 배정
        ProjectAssignment willActivate = ProjectAssignment.createForProject(
                project, userB, "운영",
                TODAY.plusDays(5), TODAY.plusDays(30));
        ReflectionTestUtils.setField(willActivate, "status", AssignmentStatus.PLANNED);
        ReflectionTestUtils.setField(willActivate, "startDate", TODAY.minusDays(1));
        assignmentRepository.save(willActivate);
        em.flush();
        em.clear();

        int updated = assignmentRepository.activatePlanned(TODAY);

        assertThat(updated).isGreaterThanOrEqualTo(1);
        em.clear();
        ProjectAssignment activated = assignmentRepository.findById(willActivate.getId()).orElseThrow();
        assertThat(activated.getStatus()).isEqualTo(AssignmentStatus.ACTIVE);
    }

    // ── 집계 쿼리 ─────────────────────────────────────────────

    @Test
    void countAssignmentsByProject_프로젝트별_ACTIVE_PLANNED_집계() {
        List<Object[]> result = assignmentRepository.countAssignmentsByProject();

        assertThat(result).isNotEmpty();
        // projectId와 count 쌍
        result.forEach(row -> {
            assertThat(row[0]).isEqualTo(project.getId());
            assertThat((Long) row[1]).isGreaterThan(0);
        });
    }

    @Test
    void countActiveDistinctUsersOn_오늘_기준_투입_인원수() {
        long count = assignmentRepository.countActiveDistinctUsersOn(TODAY);
        // userA가 ACTIVE 배정 1건
        assertThat(count).isGreaterThanOrEqualTo(1);
    }
}
