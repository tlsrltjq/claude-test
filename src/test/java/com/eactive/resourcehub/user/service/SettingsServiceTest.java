package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.team.entity.Team;
import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    @Mock UserRepository userRepository;
    @Mock TeamRepository teamRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks SettingsService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.create("hong@eactive.co.kr", "encodedPassword", "홍길동",
                "hong@eactive.co.kr", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    // ── updateProfile ───────────────────────────────────────────

    @Test
    void 프로필_수정_성공() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() ->
                service.updateProfile(1L, "이순신", "010-9999-9999",
                        LocalDate.of(1991, 5, 15), "서울시 강남구"));

        assertEquals("이순신", user.getName());
        assertEquals("010-9999-9999", user.getPhone());
        assertEquals("서울시 강남구", user.getAddress());
    }

    @Test
    void 이름_null이면_기존_이름_유지() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        service.updateProfile(1L, null, null, null, null);

        assertEquals("홍길동", user.getName());
    }

    // ── updateTeam ──────────────────────────────────────────────

    @Test
    void 팀_변경_성공() {
        Team team = Team.create("개발팀", "개발");
        ReflectionTestUtils.setField(team, "id", 5L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(teamRepository.findById(5L)).thenReturn(Optional.of(team));

        service.updateTeam(1L, 5L);

        assertEquals(team, user.getTeam());
    }

    @Test
    void 팀_null로_변경_시_미배정() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        service.updateTeam(1L, null);

        assertNull(user.getTeam());
    }

    // ── changePassword ──────────────────────────────────────────

    @Test
    void 현재_비밀번호_일치_시_변경_성공() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPw", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPw")).thenReturn("encodedNew");

        boolean result = service.changePassword(1L, "currentPw", "newPw");

        assertTrue(result);
        assertEquals("encodedNew", user.getPassword());
    }

    @Test
    void 현재_비밀번호_불일치_시_변경_실패() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPw", "encodedPassword")).thenReturn(false);

        boolean result = service.changePassword(1L, "wrongPw", "newPw");

        assertFalse(result);
        assertEquals("encodedPassword", user.getPassword());
    }
}
