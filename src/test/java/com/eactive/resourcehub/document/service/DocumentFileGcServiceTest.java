package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentFileGcServiceTest {

    @Mock DocumentRepository        documentRepository;
    @Mock DocumentVersionRepository documentVersionRepository;
    @Mock FileStorage               fileStorage;

    @InjectMocks DocumentFileGcService gcService;

    private User   owner;
    private Folder folder;
    private Document document;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(gcService, "retentionDays", 7);

        owner = User.create("owner@test.com", "encoded", "홍길동",
                "owner@test.com", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(owner, "id", 1L);

        folder = Folder.create(owner, "개인폴더");
        ReflectionTestUtils.setField(folder, "id", 10L);

        document = Document.create(folder, DocumentType.RESUME, "이력서");
        ReflectionTestUtils.setField(document, "id", 100L);
    }

    // ── runGc ────────────────────────────────────────────────────

    @Test
    void runGc_대상_없으면_0_반환() {
        when(documentRepository.findPurgeCandidates(any())).thenReturn(List.of());

        int result = gcService.runGc();

        assertThat(result).isZero();
        verifyNoInteractions(fileStorage);
    }

    @Test
    void runGc_대상_있으면_파일_삭제하고_purged_마킹() throws IOException {
        document.delete(1L);
        when(documentRepository.findPurgeCandidates(any())).thenReturn(List.of(document));

        DocumentVersion version = DocumentVersion.create(document, 1, "a.pdf", "stored.pdf",
                "2024/01/stored.pdf", 1024L, "application/pdf", "abc", owner);
        when(documentVersionRepository.findByDocumentIdIn(List.of(100L)))
                .thenReturn(List.of(version));
        when(documentRepository.save(any())).thenReturn(document);

        int result = gcService.runGc();

        assertThat(result).isEqualTo(1);
        verify(fileStorage).delete("2024/01/stored.pdf");
        verify(documentRepository).save(argThat(d -> d.getFilesPurgedAt() != null));
    }

    @Test
    void runGc_파일_삭제_실패해도_purged_마킹_계속() throws IOException {
        document.delete(1L);
        when(documentRepository.findPurgeCandidates(any())).thenReturn(List.of(document));

        DocumentVersion version = DocumentVersion.create(document, 1, "a.pdf", "stored.pdf",
                "2024/01/stored.pdf", 1024L, "application/pdf", "abc", owner);
        when(documentVersionRepository.findByDocumentIdIn(any())).thenReturn(List.of(version));
        doThrow(new IOException("disk error")).when(fileStorage).delete(anyString());
        when(documentRepository.save(any())).thenReturn(document);

        int result = gcService.runGc();

        assertThat(result).isEqualTo(1);
        verify(documentRepository).save(any());
    }

    @Test
    void runGc_preview_경로도_삭제() throws IOException {
        document.delete(1L);
        when(documentRepository.findPurgeCandidates(any())).thenReturn(List.of(document));

        DocumentVersion version = DocumentVersion.create(document, 1, "a.docx", "stored.docx",
                "2024/01/stored.docx", 1024L, "application/vnd.openxmlformats", "abc", owner);
        version.setPreview("preview.pdf", "2024/01/preview.pdf");
        when(documentVersionRepository.findByDocumentIdIn(any())).thenReturn(List.of(version));
        when(documentRepository.save(any())).thenReturn(document);

        gcService.runGc();

        verify(fileStorage).delete("2024/01/stored.docx");
        verify(fileStorage).delete("2024/01/preview.pdf");
    }

    // ── runOrphanScan ─────────────────────────────────────────────

    @Test
    void orphanScan_listAll_예외이면_0_반환() throws IOException {
        when(fileStorage.listAll(any())).thenThrow(new IOException("fs error"));

        int result = gcService.runOrphanScan();

        assertThat(result).isZero();
    }

    @Test
    void orphanScan_파일_없으면_0_반환() throws IOException {
        when(fileStorage.listAll(any())).thenReturn(List.of());

        int result = gcService.runOrphanScan();

        assertThat(result).isZero();
    }

    @Test
    void orphanScan_DB에_없는_파일이면_삭제() throws IOException {
        when(fileStorage.listAll(any())).thenReturn(List.of("orphan.pdf"));
        when(documentVersionRepository.findAllStoragePaths()).thenReturn(List.of());
        when(documentVersionRepository.findAllPreviewPaths()).thenReturn(List.of());
        when(documentVersionRepository.findAllThumbnailPaths()).thenReturn(List.of());

        int result = gcService.runOrphanScan();

        assertThat(result).isEqualTo(1);
        verify(fileStorage).delete("orphan.pdf");
    }

    @Test
    void orphanScan_DB에_있는_파일은_삭제하지_않음() throws IOException {
        when(fileStorage.listAll(any())).thenReturn(List.of("known.pdf"));
        when(documentVersionRepository.findAllStoragePaths()).thenReturn(List.of("known.pdf"));
        when(documentVersionRepository.findAllPreviewPaths()).thenReturn(List.of());
        when(documentVersionRepository.findAllThumbnailPaths()).thenReturn(List.of());

        int result = gcService.runOrphanScan();

        assertThat(result).isZero();
        verify(fileStorage, never()).delete(anyString());
    }

    // ── previewGc ─────────────────────────────────────────────────

    @Test
    void previewGc_대상_없으면_빈_리스트() {
        when(documentRepository.findPurgeCandidates(any())).thenReturn(List.of());

        var preview = gcService.previewGc();

        assertThat(preview).isEmpty();
    }

    @Test
    void previewGc_대상_있으면_GcPreviewItem_반환() {
        document.delete(1L);
        when(documentRepository.findPurgeCandidates(any())).thenReturn(List.of(document));

        DocumentVersion version = DocumentVersion.create(document, 1, "a.pdf", "stored.pdf",
                "2024/01/stored.pdf", 1024L, "application/pdf", "abc", owner);
        when(documentVersionRepository.findByDocumentIdIn(List.of(100L)))
                .thenReturn(List.of(version));

        var preview = gcService.previewGc();

        assertThat(preview).hasSize(1);
        assertThat(preview.get(0).documentId()).isEqualTo(100L);
        assertThat(preview.get(0).title()).isEqualTo("이력서");
        assertThat(preview.get(0).versionCount()).isEqualTo(1);
        assertThat(preview.get(0).fileCount()).isEqualTo(1);
    }
}
