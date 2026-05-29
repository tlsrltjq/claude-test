package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThumbnailServiceTest {

    @Mock FileStorage               fileStorage;
    @Mock DocumentVersionRepository documentVersionRepository;
    @Mock AuditService              auditService;
    @Mock HttpServletRequest        httpRequest;

    @InjectMocks ThumbnailService thumbnailService;

    private User           owner;
    private DocumentVersion version;

    @BeforeEach
    void setUp() {
        owner = User.create("owner@test.com", "encoded", "홍길동",
                "owner@test.com", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(owner, "id", 10L);

        Folder folder = Folder.create(owner, "개인폴더");
        ReflectionTestUtils.setField(folder, "id", 1L);

        Document doc = Document.create(folder, DocumentType.RESUME, "이력서");
        ReflectionTestUtils.setField(doc, "id", 100L);

        version = DocumentVersion.create(doc, 1, "resume.pdf", "stored.pdf",
                "2024/01/stored.pdf", 1024L, "application/pdf", "abc", owner);
        ReflectionTestUtils.setField(version, "id", 200L);
    }

    // ── regenerate 권한 확인 ──────────────────────────────────────

    @Test
    void regenerate_소유자도_아닌_EMPLOYEE이면_403() {
        User other = User.create("other@test.com", "encoded", "타인",
                "other@test.com", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(other, "id", 99L);
        // role default = EMPLOYEE

        CustomUserDetails userDetails = new CustomUserDetails(other);
        when(documentVersionRepository.findByIdWithDocumentAndFolder(200L))
                .thenReturn(Optional.of(version));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> thumbnailService.regenerate(200L, userDetails, httpRequest));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void regenerate_ADMIN이면_소유자_아니어도_허용() {
        User admin = User.create("admin@test.com", "encoded", "관리자",
                "admin@test.com", null, Position.REPRESENTATIVE,
                LocalDate.of(1970, 1, 1), "");
        ReflectionTestUtils.setField(admin, "id", 1L);
        admin.changeRole(UserRole.ADMIN);

        CustomUserDetails userDetails = new CustomUserDetails(admin);
        when(documentVersionRepository.findByIdWithDocumentAndFolder(200L))
                .thenReturn(Optional.of(version));

        assertDoesNotThrow(() -> thumbnailService.regenerate(200L, userDetails, httpRequest));
    }

    @Test
    void regenerate_본인이면_허용() {
        CustomUserDetails userDetails = new CustomUserDetails(owner);
        when(documentVersionRepository.findByIdWithDocumentAndFolder(200L))
                .thenReturn(Optional.of(version));

        assertDoesNotThrow(() -> thumbnailService.regenerate(200L, userDetails, httpRequest));
    }

    @Test
    void regenerate_버전_없으면_404() {
        when(documentVersionRepository.findByIdWithDocumentAndFolder(999L))
                .thenReturn(Optional.empty());
        CustomUserDetails userDetails = new CustomUserDetails(owner);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> thumbnailService.regenerate(999L, userDetails, httpRequest));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void regenerate_기존_썸네일_있으면_삭제_시도() throws IOException {
        version.setThumbnail("thumb.png", "2024/01/thumb.png", "image/png");
        CustomUserDetails userDetails = new CustomUserDetails(owner);
        when(documentVersionRepository.findByIdWithDocumentAndFolder(200L))
                .thenReturn(Optional.of(version));

        thumbnailService.regenerate(200L, userDetails, httpRequest);

        verify(fileStorage).delete("2024/01/thumb.png");
    }

    @Test
    void regenerate_기존_썸네일_삭제_실패해도_계속() throws IOException {
        version.setThumbnail("thumb.png", "2024/01/thumb.png", "image/png");
        CustomUserDetails userDetails = new CustomUserDetails(owner);
        when(documentVersionRepository.findByIdWithDocumentAndFolder(200L))
                .thenReturn(Optional.of(version));
        doThrow(new IOException("fs error")).when(fileStorage).delete(anyString());

        assertDoesNotThrow(() -> thumbnailService.regenerate(200L, userDetails, httpRequest));
    }

    @Test
    void regenerate_후_썸네일_필드_클리어됨() {
        version.setThumbnail("old.png", "2024/01/old.png", "image/png");
        assertNotNull(version.getThumbnailStoragePath());

        CustomUserDetails userDetails = new CustomUserDetails(owner);
        when(documentVersionRepository.findByIdWithDocumentAndFolder(200L))
                .thenReturn(Optional.of(version));

        thumbnailService.regenerate(200L, userDetails, httpRequest);

        assertNull(version.getThumbnailStoragePath());
        assertNull(version.getThumbnailFileName());
    }
}
