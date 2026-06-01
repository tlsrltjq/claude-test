package com.eactive.resourcehub.project.repository;

import com.eactive.resourcehub.common.config.JpaAuditingConfig;
import com.eactive.resourcehub.project.entity.Project;
import com.eactive.resourcehub.project.entity.ProjectStatus;
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
class ProjectRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Autowired TestEntityManager em;
    @Autowired ProjectRepository projectRepository;

    private static final LocalDate TODAY = LocalDate.now();

    private Project activeProject;   // 진행 중
    private Project plannedProject;  // 미래 시작
    private Project endedProject;    // 종료됨
    private Project cancelledProject; // 취소됨

    @BeforeEach
    void setUp() {
        // ACTIVE: 이미 시작, 아직 종료 안 됨
        activeProject = projectRepository.save(
                Project.create("진행중 프로젝트", "고객A",
                        TODAY.minusDays(30), TODAY.plusDays(30), null));

        // PLANNED: 미래에 시작 (status가 자동으로 PLANNED)
        plannedProject = projectRepository.save(
                Project.create("예정 프로젝트", "고객B",
                        TODAY.plusDays(10), TODAY.plusDays(60), null));

        // ENDED: 이미 종료 (status 강제 설정)
        Project ended = Project.create("종료 프로젝트", "고객C",
                TODAY.minusDays(60), TODAY.minusDays(10), null);
        ReflectionTestUtils.setField(ended, "status", ProjectStatus.ENDED);
        endedProject = projectRepository.save(ended);

        // CANCELLED: 취소 (status 강제 설정)
        Project cancelled = Project.create("취소 프로젝트", "고객D",
                TODAY.minusDays(20), TODAY.plusDays(20), null);
        ReflectionTestUtils.setField(cancelled, "status", ProjectStatus.CANCELLED);
        cancelledProject = projectRepository.save(cancelled);

        em.flush();
        em.clear();
    }

    // ── findForMonth ──────────────────────────────────────────

    @Test
    void findForMonth_이번달에_겹치는_프로젝트_반환() {
        LocalDate monthStart = TODAY.withDayOfMonth(1);
        LocalDate monthEnd   = TODAY.withDayOfMonth(TODAY.lengthOfMonth());

        List<Project> result = projectRepository.findForMonth(monthStart, monthEnd);

        // activeProject와 cancelledProject는 이번 달과 겹침 (CANCELLED 포함)
        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(p -> p.getId().equals(activeProject.getId()));
    }

    @Test
    void findForMonth_겹치지_않는_프로젝트는_제외() {
        LocalDate farFuture = TODAY.plusYears(2);
        List<Project> result = projectRepository.findForMonth(
                farFuture.withDayOfMonth(1),
                farFuture.withDayOfMonth(farFuture.lengthOfMonth()));

        assertThat(result).isEmpty();
    }

    // ── findActiveOn ──────────────────────────────────────────

    @Test
    void findActiveOn_오늘_기준_진행중_프로젝트_반환() {
        List<Project> result = projectRepository.findActiveOn(TODAY);

        assertThat(result).anyMatch(p -> p.getId().equals(activeProject.getId()));
        // CANCELLED 제외
        assertThat(result).noneMatch(p -> p.getId().equals(cancelledProject.getId()));
    }

    @Test
    void findActiveOn_ENDED_CANCELLED_제외() {
        List<Project> result = projectRepository.findActiveOn(TODAY);

        result.forEach(p -> assertThat(p.getStatus()).isNotIn(
                ProjectStatus.CANCELLED, ProjectStatus.ENDED));
    }

    // ── findAllNonCancelled ───────────────────────────────────

    @Test
    void findAllNonCancelled_취소된_프로젝트_제외() {
        List<Project> result = projectRepository.findAllNonCancelled();

        assertThat(result).noneMatch(p -> p.getStatus() == ProjectStatus.CANCELLED);
        assertThat(result).hasSize(3); // active + planned + ended
    }

    // ── findNonCancelledSince ─────────────────────────────────

    @Test
    void findNonCancelledSince_최근_종료_포함() {
        // endedProject.endDate = TODAY - 10, since = TODAY - 15 이면 포함
        List<Project> result = projectRepository.findNonCancelledSince(TODAY.minusDays(15));

        assertThat(result).anyMatch(p -> p.getId().equals(endedProject.getId()));
        assertThat(result).noneMatch(p -> p.getId().equals(cancelledProject.getId()));
    }

    @Test
    void findNonCancelledSince_오래된_종료는_제외() {
        // endedProject.endDate = TODAY - 10, since = TODAY - 5 이면 제외
        List<Project> result = projectRepository.findNonCancelledSince(TODAY.minusDays(5));

        assertThat(result).noneMatch(p -> p.getId().equals(endedProject.getId()));
    }

    // ── @Modifying 상태 전환 ──────────────────────────────────

    @Test
    void expireActive_endDate_경과한_ACTIVE_프로젝트_ENDED로_전환() {
        // endDate를 어제로 설정한 ACTIVE 프로젝트 추가
        Project willExpire = Project.create("만료될 프로젝트", "고객E",
                TODAY.minusDays(10), TODAY.minusDays(1), null);
        projectRepository.save(willExpire);
        em.flush();
        em.clear();

        int updated = projectRepository.expireActive(TODAY);

        assertThat(updated).isGreaterThanOrEqualTo(1);
        em.clear();
        Project expired = projectRepository.findById(willExpire.getId()).orElseThrow();
        assertThat(expired.getStatus()).isEqualTo(ProjectStatus.ENDED);
    }

    @Test
    void activatePlanned_startDate_도달한_PLANNED_프로젝트_ACTIVE로_전환() {
        // startDate를 어제로 설정한 PLANNED 프로젝트 추가
        Project willActivate = Project.create("활성화될 프로젝트", "고객F",
                TODAY.plusDays(5), TODAY.plusDays(30), null);
        ReflectionTestUtils.setField(willActivate, "status", ProjectStatus.PLANNED);
        ReflectionTestUtils.setField(willActivate, "startDate", TODAY.minusDays(1));
        projectRepository.save(willActivate);
        em.flush();
        em.clear();

        int updated = projectRepository.activatePlanned(TODAY);

        assertThat(updated).isGreaterThanOrEqualTo(1);
        em.clear();
        Project activated = projectRepository.findById(willActivate.getId()).orElseThrow();
        assertThat(activated.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    }
}
