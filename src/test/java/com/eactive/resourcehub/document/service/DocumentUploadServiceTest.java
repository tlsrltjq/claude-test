package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.dto.DocumentUploadRequest;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentReviewStatus;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DocumentUploadService 단위 테스트.
 * 파일 검증(확장자·magic bytes), 중복 체크, 대용량 검토 플래그 로직을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentUploadServiceTest {

    @Mock FileStorage              fileStorage;
    @Mock FolderRepository         folderRepository;
    @Mock DocumentRepository       documentRepository;
    @Mock DocumentVersionRepository documentVersionRepository;
    @Mock UserRepository           userRepository;
    @Mock AuditService             auditService;
    @Mock ThumbnailService         thumbnailService;
    @Mock HttpServletRequest       httpReq;

    @InjectMocks DocumentUploadService uploadService;

    private User owner;
    private Folder personalFolder;

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(uploadService, "allowedExtensionsRaw",
                "pdf,jpg,jpeg,png,docx,hwp,hwpx,zip,xlsx,xls");

        owner = User.create("owner@test.com", "encoded", "홍길동",
                "owner@test.com", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(owner, "id", 1L);

        personalFolder = Folder.create(owner, "개인폴더");
        ReflectionTestUtils.setField(personalFolder, "id", 10L);

        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.of(personalFolder));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(fileStorage.store(any(), any(), any())).thenReturn("2024/01/stored.pdf");
        when(documentVersionRepository.countByDocumentId(any())).thenReturn(0);
        when(documentVersionRepository.findFirstByChecksumInFolder(any(), any(), any()))
                .thenReturn(Optional.empty());
    }

    // ── 파일 검증 ────────────────────────────────────────────────

    @Test
    void 빈_파일이면_예외() {
        DocumentUploadRequest req = makeRequest(emptyFile("doc.pdf"));
        assertThrows(IllegalArgumentException.class,
                () -> uploadService.upload(1L, req, httpReq));
    }

    @Test
    void 허용되지_않은_확장자이면_예외() {
        DocumentUploadRequest req = makeRequest(pdfBytes("malware.exe", "exe"));
        assertThrows(IllegalArgumentException.class,
                () -> uploadService.upload(1L, req, httpReq));
    }

    @Test
    void 확장자와_magic_bytes_불일치이면_예외() {
        // PNG 헤더를 pdf 확장자로 위장
        byte[] pngMagic = {(byte)0x89, 0x50, 0x4E, 0x47};
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.pdf", "application/pdf", pngMagic);
        DocumentUploadRequest req = makeRequest(file);
        assertThrows(IllegalArgumentException.class,
                () -> uploadService.upload(1L, req, httpReq));
    }

    // ── 신규 업로드 ──────────────────────────────────────────────

    @Test
    void 신규_문서_업로드_성공() {
        Document doc = Document.create(personalFolder, DocumentType.RESUME, "이력서");
        ReflectionTestUtils.setField(doc, "id", 100L);

        when(documentRepository.findByFolderIdAndDocumentTypeAndTitle(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(documentRepository.saveAndFlush(any())).thenReturn(doc);

        Document result = uploadService.upload(1L, makeRequest(validPdfFile()), httpReq);

        assertNotNull(result);
        verify(documentRepository).saveAndFlush(any());
        verify(documentVersionRepository).save(any());
    }

    // ── 대용량 파일 검토 플래그 ───────────────────────────────────

    @Test
    void 소용량_파일이면_자동승인() {
        Document doc = Document.create(personalFolder, DocumentType.RESUME, "이력서");
        ReflectionTestUtils.setField(doc, "id", 100L);
        when(documentRepository.findByFolderIdAndDocumentTypeAndTitle(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(documentRepository.saveAndFlush(any())).thenReturn(doc);

        // 1KB 파일(20MB 미만) → 자동승인
        MockMultipartFile small = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf",
                buildPdfBytes(1024));

        uploadService.upload(1L, makeRequest(small), httpReq);

        verify(documentVersionRepository).save(argThat(v ->
                ((DocumentVersion) v).getReviewStatus() == DocumentReviewStatus.APPROVED));
    }

    @Test
    void 대용량_파일이면_검토대기() {
        Document doc = Document.create(personalFolder, DocumentType.RESUME, "이력서");
        ReflectionTestUtils.setField(doc, "id", 100L);
        when(documentRepository.findByFolderIdAndDocumentTypeAndTitle(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(documentRepository.saveAndFlush(any())).thenReturn(doc);

        // 21MB 파일(20MB 초과) → 검토 대기
        MockMultipartFile large = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf",
                buildPdfBytes(21 * 1024 * 1024));

        uploadService.upload(1L, makeRequest(large), httpReq);

        verify(documentVersionRepository).save(argThat(v ->
                ((DocumentVersion) v).getReviewStatus() == DocumentReviewStatus.PENDING_REVIEW));
    }

    // ── 중복 체크 ────────────────────────────────────────────────

    @Test
    void 동일_체크섬_파일이면_예외() {
        Document existing = Document.create(personalFolder, DocumentType.RESUME, "이력서");
        DocumentVersion existingVer = DocumentVersion.create(
                existing, 1, "resume.pdf", "uuid.pdf", "2024/01/uuid.pdf",
                1024L, "application/pdf", "checksum123", owner);
        when(documentVersionRepository.findFirstByChecksumInFolder(any(), any(), any()))
                .thenReturn(Optional.of(existingVer));

        assertThrows(IllegalArgumentException.class,
                () -> uploadService.upload(1L, makeRequest(validPdfFile()), httpReq));
    }

    // ── 헬퍼 ────────────────────────────────────────────────────

    private DocumentUploadRequest makeRequest(MultipartFile file) {
        DocumentUploadRequest req = new DocumentUploadRequest();
        req.setFile(file);
        req.setDocumentType(DocumentType.RESUME);
        req.setTitle("이력서");
        return req;
    }

    private MockMultipartFile emptyFile(String name) {
        return new MockMultipartFile("file", name, "application/pdf", new byte[0]);
    }

    private MockMultipartFile pdfBytes(String filename, String ext) {
        return new MockMultipartFile("file", filename, "application/pdf", buildPdfBytes(100));
    }

    private MockMultipartFile validPdfFile() {
        return new MockMultipartFile("file", "resume.pdf", "application/pdf", buildPdfBytes(1024));
    }

    private byte[] buildPdfBytes(int size) {
        byte[] data = new byte[Math.max(size, 5)];
        // PDF magic bytes: %PDF-
        data[0] = 0x25; data[1] = 0x50; data[2] = 0x44; data[3] = 0x46; data[4] = 0x2D;
        return data;
    }
}
