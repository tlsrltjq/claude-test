package com.eactive.resourcehub.document.entity;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentVersionTest {

    private DocumentVersion version(String fileName, String previewStoragePath) {
        DocumentVersion v = new DocumentVersion() {};
        ReflectionTestUtils.setField(v, "originalFileName", fileName);
        ReflectionTestUtils.setField(v, "previewStoragePath", previewStoragePath);
        return v;
    }

    // ── 항상 true — 직접 지원 형식 ───────────────────────────

    @Test
    void PDF는_미리보기_지원() {
        assertThat(version("resume.pdf", null).isPreviewSupported()).isTrue();
    }

    @Test
    void JPG는_미리보기_지원() {
        assertThat(version("photo.jpg", null).isPreviewSupported()).isTrue();
    }

    @Test
    void JPEG는_미리보기_지원() {
        assertThat(version("photo.jpeg", null).isPreviewSupported()).isTrue();
    }

    @Test
    void PNG는_미리보기_지원() {
        assertThat(version("img.png", null).isPreviewSupported()).isTrue();
    }

    // ── previewStoragePath 있을 때만 true — 오피스 형식 ──────

    @Test
    void DOCX_previewPath_있으면_미리보기_지원() {
        assertThat(version("doc.docx", "previews/doc.pdf").isPreviewSupported()).isTrue();
    }

    @Test
    void DOCX_previewPath_없으면_미리보기_미지원() {
        assertThat(version("doc.docx", null).isPreviewSupported()).isFalse();
    }

    @Test
    void HWPX_previewPath_있으면_미리보기_지원() {
        assertThat(version("doc.hwpx", "previews/doc.pdf").isPreviewSupported()).isTrue();
    }

    @Test
    void PPTX_previewPath_있으면_미리보기_지원() {
        assertThat(version("slide.pptx", "previews/slide.pdf").isPreviewSupported()).isTrue();
    }

    @Test
    void PPT_previewPath_없으면_미리보기_미지원() {
        assertThat(version("slide.ppt", null).isPreviewSupported()).isFalse();
    }

    @Test
    void XLSX_previewPath_있으면_미리보기_지원() {
        assertThat(version("sheet.xlsx", "previews/sheet.pdf").isPreviewSupported()).isTrue();
    }

    @Test
    void XLS_previewPath_없으면_미리보기_미지원() {
        assertThat(version("sheet.xls", null).isPreviewSupported()).isFalse();
    }

    // ── 항상 false — 미지원 형식 ─────────────────────────────

    @Test
    void HWP는_previewPath_있어도_미리보기_미지원() {
        // hwp는 LibreOffice 변환 불안정으로 제외
        assertThat(version("doc.hwp", "previews/doc.pdf").isPreviewSupported()).isFalse();
        assertThat(version("doc.hwp", null).isPreviewSupported()).isFalse();
    }

    @Test
    void ZIP은_미리보기_미지원() {
        assertThat(version("archive.zip", null).isPreviewSupported()).isFalse();
    }

    @Test
    void 알_수_없는_확장자는_미리보기_미지원() {
        assertThat(version("data.bin", null).isPreviewSupported()).isFalse();
        assertThat(version("noextension", null).isPreviewSupported()).isFalse();
    }
}
