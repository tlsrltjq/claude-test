package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.*;
import com.eactive.resourcehub.document.repository.DocumentRepository;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentDeleteServiceTest {

    @Mock DocumentRepository documentRepository;
    @Mock DocumentVersionRepository documentVersionRepository;
    @Mock AuditService auditService;
    @Mock HttpServletRequest request;

    @InjectMocks DocumentDeleteService service;

    private User owner;
    private User admin;
    private User other;
    private Folder folder;
    private Document document;

    @BeforeEach
    void setUp() {
        owner = makeUser(1L, UserRole.EMPLOYEE);
        admin = makeUser(2L, UserRole.ADMIN);
        other = makeUser(3L, UserRole.EMPLOYEE);

        folder = Folder.create(owner, "개인 폴더");
        document = Document.create(folder, DocumentType.RESUME, "이력서");
        ReflectionTestUtils.setField(document, "id", 10L);

        when(documentRepository.findByIdForDetail(10L)).thenReturn(Optional.of(document));
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(documentRepository.save(any())).thenReturn(document);
    }

    // ── deleteDocument (ADMIN 전용) ─────────────────────────────

    @Test
    void ADMIN은_문서_삭제_가능() {
        assertDoesNotThrow(() ->
                service.deleteDocument(10L, details(admin), request));
        verify(documentRepository).save(any());
    }

    @Test
    void 비ADMIN은_문서_삭제_시_403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteDocument(10L, details(owner), request));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(documentRepository, never()).save(any());
    }

    @Test
    void 존재하지_않는_문서_삭제_시_404() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteDocument(99L, details(admin), request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ── deleteOwnDocument ────────────────────────────────────────

    @Test
    void 본인_문서_삭제_성공() {
        assertDoesNotThrow(() ->
                service.deleteOwnDocument(10L, 1L, request));
        verify(documentRepository).save(any());
    }

    @Test
    void 타인_문서_삭제_시_403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteOwnDocument(10L, 3L, request));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(documentRepository, never()).save(any());
    }

    @Test
    void 존재하지_않는_본인_문서_삭제_시_404() {
        when(documentRepository.findByIdForDetail(99L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteOwnDocument(99L, 1L, request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ── deletePublicFolderDocument ───────────────────────────────

    @Test
    void ADMIN은_공용폴더_타인_문서도_삭제_가능() {
        assertDoesNotThrow(() ->
                service.deletePublicFolderDocument(10L, 2L, UserRole.ADMIN, request));
        verify(documentRepository).save(any());
    }

    @Test
    void 본인_업로드_문서는_삭제_가능() {
        DocumentVersion version = DocumentVersion.create(
                document, 1, "파일.pdf", "uuid.pdf", "/storage/uuid.pdf",
                1024L, "application/pdf", "checksum", owner);
        when(documentVersionRepository.findByDocumentIdOrderByVersionNoDesc(10L))
                .thenReturn(List.of(version));

        assertDoesNotThrow(() ->
                service.deletePublicFolderDocument(10L, 1L, UserRole.EMPLOYEE, request));
        verify(documentRepository).save(any());
    }

    @Test
    void 타인_업로드_문서는_EMPLOYEE가_삭제_불가() {
        DocumentVersion version = DocumentVersion.create(
                document, 1, "파일.pdf", "uuid.pdf", "/storage/uuid.pdf",
                1024L, "application/pdf", "checksum", owner);
        when(documentVersionRepository.findByDocumentIdOrderByVersionNoDesc(10L))
                .thenReturn(List.of(version));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deletePublicFolderDocument(10L, 3L, UserRole.EMPLOYEE, request));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────

    private User makeUser(long id, UserRole role) {
        User u = User.create("user" + id + "@eactive.co.kr", "encoded", "사용자" + id,
                "user" + id + "@eactive.co.kr", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(u, "id", id);
        if (role != UserRole.EMPLOYEE) {
            ReflectionTestUtils.setField(u, "role", role);
        }
        return u;
    }

    private CustomUserDetails details(User user) {
        return new CustomUserDetails(user);
    }
}
