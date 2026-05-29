package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.team.entity.Team;
import com.eactive.resourcehub.team.repository.TeamRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeManagementServiceTest {

    @Mock UserRepository             userRepository;
    @Mock TeamRepository             teamRepository;
    @Mock FolderRepository           folderRepository;
    @Mock DocumentRepository         documentRepository;
    @Mock DocumentVersionRepository  documentVersionRepository;
    @Mock AuditService               auditService;
    @Mock HttpServletRequest         httpRequest;

    @InjectMocks EmployeeManagementService service;

    private User activeEmployee;
    private User adminUser;

    @BeforeEach
    void setUp() {
        activeEmployee = User.create("emp@test.com", "encoded", "직원",
                "emp@test.com", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(activeEmployee, "id", 10L);
        activeEmployee.verifyEmail();

        adminUser = User.create("admin@test.com", "encoded", "관리자",
                "admin@test.com", null, Position.REPRESENTATIVE,
                LocalDate.of(1970, 1, 1), "");
        ReflectionTestUtils.setField(adminUser, "id", 1L);
        adminUser.changeRole(UserRole.ADMIN);
        adminUser.verifyEmail();
    }

    // ── toggleStatus ─────────────────────────────────────────────

    @Test
    void toggleStatus_ADMIN은_비활성화_불가() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThrows(IllegalArgumentException.class,
                () -> service.toggleStatus(1L, 99L, httpRequest));
    }

    @Test
    void toggleStatus_ACTIVE이면_DISABLED로_변경() {
        assertThat(activeEmployee.getStatus()).isEqualTo(UserStatus.ACTIVE);
        when(userRepository.findById(10L)).thenReturn(Optional.of(activeEmployee));

        UserStatus result = service.toggleStatus(10L, 1L, httpRequest);

        assertThat(result).isEqualTo(UserStatus.DISABLED);
        assertThat(activeEmployee.getStatus()).isEqualTo(UserStatus.DISABLED);
    }

    @Test
    void toggleStatus_DISABLED이면_ACTIVE로_변경() {
        activeEmployee.disable();
        assertThat(activeEmployee.getStatus()).isEqualTo(UserStatus.DISABLED);
        when(userRepository.findById(10L)).thenReturn(Optional.of(activeEmployee));

        UserStatus result = service.toggleStatus(10L, 1L, httpRequest);

        assertThat(result).isEqualTo(UserStatus.ACTIVE);
        assertThat(activeEmployee.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void toggleStatus_사용자_없으면_예외() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.toggleStatus(999L, 1L, httpRequest));
    }

    // ── deleteEmployee ────────────────────────────────────────────

    @Test
    void deleteEmployee_ADMIN은_삭제_불가() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThrows(IllegalArgumentException.class,
                () -> service.deleteEmployee(1L, 99L, httpRequest));
    }

    @Test
    void deleteEmployee_자신은_삭제_불가() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(activeEmployee));

        assertThrows(IllegalArgumentException.class,
                () -> service.deleteEmployee(10L, 10L, httpRequest));
    }

    @Test
    void deleteEmployee_성공이면_삭제_감사로그_기록() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(activeEmployee));

        service.deleteEmployee(10L, 1L, httpRequest);

        verify(userRepository).deleteById(10L);
        verify(auditService).log(eq(1L), any(), any(), eq(10L), anyString(), eq(httpRequest));
    }

    // ── changeTeam ────────────────────────────────────────────────

    @Test
    void changeTeam_사용자_없으면_예외() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.changeTeam(999L, 5L, 1L, httpRequest));
    }

    @Test
    void changeTeam_팀_없으면_예외() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(activeEmployee));
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.changeTeam(10L, 999L, 1L, httpRequest));
    }

    @Test
    void changeTeam_성공이면_팀_변경_감사로그_기록() {
        Team team = mock(Team.class);
        when(team.getName()).thenReturn("개발팀");
        when(userRepository.findById(10L)).thenReturn(Optional.of(activeEmployee));
        when(teamRepository.findById(5L)).thenReturn(Optional.of(team));

        service.changeTeam(10L, 5L, 1L, httpRequest);

        assertThat(activeEmployee.getTeam()).isEqualTo(team);
        verify(auditService).log(eq(1L), any(), any(), eq(10L), anyString(), eq(httpRequest));
    }

    // ── changePosition ────────────────────────────────────────────

    @Test
    void changePosition_사용자_없으면_예외() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.changePosition(999L, Position.MANAGER, 1L, httpRequest));
    }

    @Test
    void changePosition_성공이면_직급_변경_감사로그_기록() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(activeEmployee));

        service.changePosition(10L, Position.MANAGER, 1L, httpRequest);

        assertThat(activeEmployee.getPosition()).isEqualTo(Position.MANAGER);
        verify(auditService).log(eq(1L), any(), any(), eq(10L), anyString(), eq(httpRequest));
    }
}
