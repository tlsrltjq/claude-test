package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.user.entity.AllowedEmail;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.user.repository.AllowedEmailRepository;
import com.eactive.resourcehub.user.repository.UserRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
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

    // ── addBulkFromExcel — 보안 검증 ────────────────────────────

    @Test
    void xlsx_아닌_확장자_업로드_거부() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "emails.pdf", "application/pdf",
                new byte[]{0x25, 0x50, 0x44, 0x46});
        assertThrows(IllegalArgumentException.class,
                () -> service.addBulkFromExcel(file, UserRole.EMPLOYEE, 1L));
        verify(allowedEmailRepository, never()).save(any());
    }

    @Test
    void xlsx_위장_파일_매직바이트_불일치_거부() {
        // PDF 매직바이트를 .xlsx 확장자로 위장
        MockMultipartFile file = new MockMultipartFile(
                "file", "emails.xlsx", "application/octet-stream",
                new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D});
        assertThrows(IllegalArgumentException.class,
                () -> service.addBulkFromExcel(file, UserRole.EMPLOYEE, 1L));
        verify(allowedEmailRepository, never()).save(any());
    }

    @Test
    void 행_1000초과_업로드_거부() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            for (int i = 0; i <= 1001; i++) {
                sheet.createRow(i).createCell(0).setCellValue("e" + i + "@test.com");
            }
            wb.write(out);
        }
        MockMultipartFile file = new MockMultipartFile(
                "file", "emails.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                out.toByteArray());
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        assertThrows(IllegalArgumentException.class,
                () -> service.addBulkFromExcel(file, UserRole.EMPLOYEE, 1L));
    }
}
