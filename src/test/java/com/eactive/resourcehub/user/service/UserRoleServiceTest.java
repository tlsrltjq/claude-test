package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.audit.service.AuditLogService;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserRoleServiceTest {

    @Mock UserRepository userRepository;
    @Mock AuditLogService auditLogService;
    @Mock HttpServletRequest request;

    @InjectMocks UserRoleService service;

    private User target;

    @BeforeEach
    void setUp() {
        target = User.create("emp@eactive.co.kr", "encoded", "직원",
                "emp@eactive.co.kr", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(target, "id", 10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(target));
    }

    @Test
    void EMPLOYEE에서_SALES로_변경_성공() {
        service.changeRole(10L, UserRole.SALES, 1L, request);
        assertEquals(UserRole.SALES, target.getRole());
    }

    @Test
    void EMPLOYEE에서_ADMIN으로_변경_성공() {
        service.changeRole(10L, UserRole.ADMIN, 1L, request);
        assertEquals(UserRole.ADMIN, target.getRole());
    }

    @Test
    void SALES에서_EMPLOYEE로_변경_성공() {
        ReflectionTestUtils.setField(target, "role", UserRole.SALES);
        service.changeRole(10L, UserRole.EMPLOYEE, 1L, request);
        assertEquals(UserRole.EMPLOYEE, target.getRole());
    }

    @Test
    void TEAM_LEADER로_변경_시_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changeRole(10L, UserRole.TEAM_LEADER, 1L, request));
        assertEquals(UserRole.EMPLOYEE, target.getRole());
    }

    @Test
    void 존재하지_않는_사용자_역할_변경_시_예외() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.changeRole(99L, UserRole.SALES, 1L, request));
    }
}
