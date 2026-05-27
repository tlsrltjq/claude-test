package com.eactive.resourcehub.team.service;

import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.team.dto.TeamRequest;
import com.eactive.resourcehub.team.entity.Team;
import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TeamServiceTest {

    @Mock TeamRepository teamRepository;
    @Mock UserRepository userRepository;
    @Mock AuditService auditService;
    @Mock HttpServletRequest request;

    @InjectMocks TeamService service;

    private Team team;

    @BeforeEach
    void setUp() {
        team = Team.create("개발팀", "개발 팀");
        ReflectionTestUtils.setField(team, "id", 1L);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
    }

    // ── create ───────────────────────────────────────────────────

    @Test
    void 중복_아닌_팀_이름_생성_성공() {
        when(teamRepository.existsByName("신규팀")).thenReturn(false);
        when(teamRepository.save(any())).thenReturn(team);

        TeamRequest req = new TeamRequest();
        req.setName("신규팀");
        assertDoesNotThrow(() -> service.create(req, 1L, request));
        verify(teamRepository).save(any());
    }

    @Test
    void 중복_팀_이름_생성_시_예외() {
        when(teamRepository.existsByName("개발팀")).thenReturn(true);

        TeamRequest req = new TeamRequest();
        req.setName("개발팀");
        assertThrows(IllegalArgumentException.class,
                () -> service.create(req, 1L, request));
        verify(teamRepository, never()).save(any());
    }

    // ── update ───────────────────────────────────────────────────

    @Test
    void 같은_이름으로_업데이트_성공() {
        when(teamRepository.existsByName("개발팀")).thenReturn(true);

        TeamRequest req = new TeamRequest();
        req.setName("개발팀");
        req.setDescription("변경된 설명");
        assertDoesNotThrow(() -> service.update(1L, req, 1L, request));
        assertEquals("개발팀", team.getName());
        assertEquals("변경된 설명", team.getDescription());
    }

    @Test
    void 다른_고유_이름으로_업데이트_성공() {
        when(teamRepository.existsByName("QA팀")).thenReturn(false);

        TeamRequest req = new TeamRequest();
        req.setName("QA팀");
        assertDoesNotThrow(() -> service.update(1L, req, 1L, request));
        assertEquals("QA팀", team.getName());
    }

    @Test
    void 중복된_다른_이름으로_업데이트_시_예외() {
        when(teamRepository.existsByName("인프라팀")).thenReturn(true);

        TeamRequest req = new TeamRequest();
        req.setName("인프라팀");
        assertThrows(IllegalArgumentException.class,
                () -> service.update(1L, req, 1L, request));
    }

    @Test
    void 존재하지_않는_팀_업데이트_시_예외() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        TeamRequest req = new TeamRequest();
        req.setName("없는팀");
        assertThrows(IllegalArgumentException.class,
                () -> service.update(99L, req, 1L, request));
    }

    // ── delete ───────────────────────────────────────────────────

    @Test
    void 소속_직원_없는_팀_삭제_성공() {
        when(userRepository.findByTeamId(1L)).thenReturn(List.of());
        assertDoesNotThrow(() -> service.delete(1L, 1L, request));
        verify(teamRepository).delete(team);
    }

    @Test
    void 소속_직원_있는_팀_삭제_시_예외() {
        User member = User.create("emp@eactive.co.kr", "pw", "직원",
                "emp@eactive.co.kr", team, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        when(userRepository.findByTeamId(1L)).thenReturn(List.of(member));

        assertThrows(IllegalArgumentException.class,
                () -> service.delete(1L, 1L, request));
        verify(teamRepository, never()).delete(any());
    }

    @Test
    void 존재하지_않는_팀_삭제_시_예외() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.delete(99L, 1L, request));
    }

    // ── toggleProjectTeam ────────────────────────────────────────

    @Test
    void 인력표_노출_토글_true에서_false() {
        assertTrue(team.isProjectTeam());
        boolean result = service.toggleProjectTeam(1L);
        assertFalse(result);
        assertFalse(team.isProjectTeam());
    }

    @Test
    void 인력표_노출_토글_false에서_true() {
        team.toggleProjectTeam();
        assertFalse(team.isProjectTeam());

        boolean result = service.toggleProjectTeam(1L);
        assertTrue(result);
        assertTrue(team.isProjectTeam());
    }
}
