package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.user.entity.AllowedEmail;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.AllowedEmailRepository;
import com.eactive.resourcehub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailAllowlistServiceTest {

    @Mock AllowedEmailRepository allowedEmailRepository;
    @Mock UserRepository userRepository;

    @InjectMocks EmailAllowlistService service;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = User.create("admin@eactive.co.kr", "encoded", "관리자",
                "admin@eactive.co.kr", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(admin, "id", 1L);
    }

    // ── isAllowed ───────────────────────────────────────────────

    @Test
    void 등록된_이메일은_허용() {
        when(allowedEmailRepository.existsByEmail("hong@eactive.co.kr")).thenReturn(true);
        assertTrue(service.isAllowed("hong@eactive.co.kr"));
    }

    @Test
    void 미등록_이메일은_거부() {
        when(allowedEmailRepository.existsByEmail("unknown@eactive.co.kr")).thenReturn(false);
        assertFalse(service.isAllowed("unknown@eactive.co.kr"));
    }

    @Test
    void 이메일_검사_시_소문자_변환() {
        when(allowedEmailRepository.existsByEmail("hong@eactive.co.kr")).thenReturn(true);
        assertTrue(service.isAllowed("HONG@EACTIVE.CO.KR"));
    }

    @Test
    void 이메일_검사_시_앞뒤_공백_제거() {
        when(allowedEmailRepository.existsByEmail("hong@eactive.co.kr")).thenReturn(true);
        assertTrue(service.isAllowed("  hong@eactive.co.kr  "));
    }

    // ── add ─────────────────────────────────────────────────────

    @Test
    void 새_이메일_추가_성공() {
        when(allowedEmailRepository.existsByEmail("new@eactive.co.kr")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        AllowedEmail entity = AllowedEmail.create("new@eactive.co.kr", "메모", null, admin);
        when(allowedEmailRepository.save(any())).thenReturn(entity);

        AllowedEmail result = service.add("new@eactive.co.kr", "메모", null, 1L);
        assertNotNull(result);
        verify(allowedEmailRepository).save(any());
    }

    @Test
    void 중복_이메일_추가_시_예외() {
        when(allowedEmailRepository.existsByEmail("dup@eactive.co.kr")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> service.add("dup@eactive.co.kr", null, null, 1L));
        verify(allowedEmailRepository, never()).save(any());
    }

    @Test
    void 추가_시_이메일_소문자_정규화() {
        when(allowedEmailRepository.existsByEmail("hong@eactive.co.kr")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        AllowedEmail entity = AllowedEmail.create("hong@eactive.co.kr", null, null, admin);
        when(allowedEmailRepository.save(any())).thenReturn(entity);

        service.add("HONG@EACTIVE.CO.KR", null, null, 1L);
        verify(allowedEmailRepository).existsByEmail("hong@eactive.co.kr");
    }

    // ── remove ──────────────────────────────────────────────────

    @Test
    void 존재하는_이메일_삭제_성공() {
        when(allowedEmailRepository.existsById(10L)).thenReturn(true);
        assertDoesNotThrow(() -> service.remove(10L));
        verify(allowedEmailRepository).deleteById(10L);
    }

    @Test
    void 존재하지_않는_이메일_삭제_시_예외() {
        when(allowedEmailRepository.existsById(99L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.remove(99L));
        verify(allowedEmailRepository, never()).deleteById(any());
    }
}
