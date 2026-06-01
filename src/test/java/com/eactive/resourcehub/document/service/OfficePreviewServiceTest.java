package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OfficePreviewServiceTest {

    @Mock FileStorage fileStorage;
    @Mock DocumentVersionRepository documentVersionRepository;
    @Mock ThumbnailService thumbnailService;

    @InjectMocks OfficePreviewService service;

    private DocumentVersion version;

    @BeforeEach
    void setUp() {
        version = mock(DocumentVersion.class);
        when(version.getId()).thenReturn(1L);
        when(version.getOriginalFileName()).thenReturn("document.docx");
        when(version.getStoragePath()).thenReturn("2024/01/uuid.docx");
    }

    // ── supports() ───────────────────────────────────────────

    @Test
    void DOCX_지원() {
        assertThat(service.supports("docx")).isTrue();
    }

    @Test
    void HWPX_지원() {
        assertThat(service.supports("hwpx")).isTrue();
    }

    @Test
    void PPTX_지원() {
        assertThat(service.supports("pptx")).isTrue();
    }

    @Test
    void PPT_지원() {
        assertThat(service.supports("ppt")).isTrue();
    }

    @Test
    void XLSX_지원() {
        assertThat(service.supports("xlsx")).isTrue();
    }

    @Test
    void XLS_지원() {
        assertThat(service.supports("xls")).isTrue();
    }

    @Test
    void HWP_미지원() {
        assertThat(service.supports("hwp")).isFalse();
    }

    @Test
    void ZIP_미지원() {
        assertThat(service.supports("zip")).isFalse();
    }

    @Test
    void PDF_미지원() {
        assertThat(service.supports("pdf")).isFalse();
    }

    // ── LibreOffice 비활성화 시 변환 skip ────────────────────

    @Test
    void libreOffice_비활성화_시_변환_없이_썸네일만_호출() throws IOException {
        ReflectionTestUtils.setField(service, "libreOfficeEnabled", false);
        when(fileStorage.load(any())).thenReturn(new ByteArrayInputStream(new byte[0]));

        service.convertAndSave(version);

        // 파일 저장 없이 썸네일 서비스만 호출됨
        verify(fileStorage, never()).store(any(), any(), any());
        verify(documentVersionRepository, never()).save(any());
        verify(thumbnailService).generateAndSave(version);
    }
}
