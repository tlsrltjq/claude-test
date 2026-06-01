package com.eactive.resourcehub.user.repository;

import com.eactive.resourcehub.common.config.JpaAuditingConfig;
import com.eactive.resourcehub.team.entity.Team;
import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class UserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Autowired TestEntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired TeamRepository teamRepository;

    private Team devTeam;
    private User adminUser;
    private User salesUser;
    private User employeeWithTeam;
    private User employeeNoTeam;

    @BeforeEach
    void setUp() {
        devTeam = teamRepository.save(Team.create("개발팀", "개발"));

        adminUser = User.create("admin@test.com", "encoded", "관리자",
                "admin@test.com", devTeam, Position.MANAGER,
                LocalDate.of(1985, 3, 1), "010-1111-1111");
        adminUser.activate();
        userRepository.save(adminUser);

        salesUser = User.create("sales@test.com", "encoded", "영업",
                "sales@test.com", null, Position.STAFF,
                LocalDate.of(1990, 5, 1), "010-2222-2222");
        salesUser.activate();
        userRepository.save(salesUser);

        employeeWithTeam = User.create("emp1@test.com", "encoded", "팀원A",
                "emp1@test.com", devTeam, Position.STAFF,
                LocalDate.of(1995, 7, 1), "010-3333-3333");
        employeeWithTeam.activate();
        userRepository.save(employeeWithTeam);

        employeeNoTeam = User.create("emp2@test.com", "encoded", "팀원B",
                "emp2@test.com", null, Position.STAFF,
                LocalDate.of(1996, 9, 1), "010-4444-4444");
        employeeNoTeam.activate();
        userRepository.save(employeeNoTeam);

        em.flush();
        em.clear();
    }

    // ── 기본 조회 ─────────────────────────────────────────────

    @Test
    void findByLoginId_존재하는_ID_반환() {
        Optional<User> result = userRepository.findByLoginId("admin@test.com");
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("관리자");
    }

    @Test
    void findByEmail_존재하는_이메일_반환() {
        Optional<User> result = userRepository.findByEmail("sales@test.com");
        assertThat(result).isPresent();
    }

    @Test
    void existsByEmail_중복_이메일_true() {
        assertThat(userRepository.existsByEmail("admin@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexist@test.com")).isFalse();
    }

    // ── JOIN FETCH 쿼리 ───────────────────────────────────────

    @Test
    void findByIdWithTeam_팀있는_유저_team_지연로딩_없음() {
        Optional<User> result = userRepository.findByIdWithTeam(adminUser.getId());
        assertThat(result).isPresent();
        // team이 FETCH 되었으므로 접근 시 LazyInitializationException 없음
        assertThat(result.get().getTeam()).isNotNull();
        assertThat(result.get().getTeam().getName()).isEqualTo("개발팀");
    }

    @Test
    void findByIdWithTeam_팀없는_유저_team이_null() {
        Optional<User> result = userRepository.findByIdWithTeam(employeeNoTeam.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getTeam()).isNull();
    }

    @Test
    void findByStatusWithTeam_ACTIVE_유저_전체_반환() {
        List<User> users = userRepository.findByStatusWithTeam(UserStatus.ACTIVE);
        assertThat(users).hasSize(4);
        // 팀 있는 유저의 team 접근 가능
        users.stream()
                .filter(u -> u.getTeam() != null)
                .forEach(u -> assertThat(u.getTeam().getName()).isNotBlank());
    }

    @Test
    void findByStatusInWithTeam_ACTIVE만_포함() {
        List<User> users = userRepository.findByStatusInWithTeam(List.of(UserStatus.ACTIVE));
        assertThat(users).hasSize(4);
    }

    // ── findFilteredPage — 복합 필터 ──────────────────────────

    @Test
    void findFilteredPage_필터_없으면_전체_반환() {
        Page<User> page = userRepository.findFilteredPage(
                List.of(UserStatus.ACTIVE), null, null, null, null,
                PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(4);
    }

    @Test
    void findFilteredPage_이름_검색() {
        Page<User> page = userRepository.findFilteredPage(
                List.of(UserStatus.ACTIVE), "%관리자%", null, null, null,
                PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("관리자");
    }

    @Test
    void findFilteredPage_직급_필터() {
        Page<User> page = userRepository.findFilteredPage(
                List.of(UserStatus.ACTIVE), null, Position.MANAGER, null, null,
                PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getPosition()).isEqualTo(Position.MANAGER);
    }

    @Test
    void findFilteredPage_팀_필터() {
        Page<User> page = userRepository.findFilteredPage(
                List.of(UserStatus.ACTIVE), null, null, null, devTeam.getId(),
                PageRequest.of(0, 10));

        // 개발팀 소속: adminUser, employeeWithTeam
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findFilteredPage_페이지네이션() {
        Page<User> page = userRepository.findFilteredPage(
                List.of(UserStatus.ACTIVE), null, null, null, null,
                PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    // ── findActiveMembersFiltered ─────────────────────────────

    @Test
    void findActiveMembersFiltered_ADMIN_제외() {
        // adminUser를 ADMIN 역할로 변경
        adminUser.changeRole(UserRole.ADMIN);
        userRepository.save(adminUser);
        em.flush();
        em.clear();

        List<User> members = userRepository.findActiveMembersFiltered(
                UserStatus.ACTIVE, UserRole.ADMIN, null, null);

        // ADMIN 제외 3명
        assertThat(members).hasSize(3);
        members.forEach(u -> assertThat(u.getRole()).isNotEqualTo(UserRole.ADMIN));
    }

    @Test
    void findActiveMembersFiltered_팀_필터() {
        List<User> members = userRepository.findActiveMembersFiltered(
                UserStatus.ACTIVE, UserRole.ADMIN, null, devTeam.getId());

        // 개발팀 소속 2명 (admin은 EMPLOYEE 역할 상태)
        assertThat(members).hasSize(2);
        members.forEach(u -> assertThat(u.getTeam()).isNotNull());
    }

    // ── 제약 조건 ─────────────────────────────────────────────

    @Test
    void 이메일_중복_저장시_예외() {
        User duplicate = User.create(
                "admin@test.com", "encoded", "중복",
                "admin@test.com", null, Position.STAFF,
                LocalDate.of(2000, 1, 1), "010-9999-9999");

        assertThatThrownBy(() -> {
            userRepository.save(duplicate);
            em.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
