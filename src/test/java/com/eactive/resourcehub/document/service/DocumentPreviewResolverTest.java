package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.document.entity.DocumentVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DocumentPreviewResolverTest {

    private DocumentPreviewResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new DocumentPreviewResolver();
    }

    private DocumentVersion versionWithName(String fileName) {
        DocumentVersion v = mock(DocumentVersion.class);
        when(v.getOriginalFileName()).thenReturn(fileName);
        return v;
    }

    private DocumentVersion versionWithNameAndPreview(String fileName, String previewPath) {
        DocumentVersion v = versionWithName(fileName);
        when(v.getPreviewStoragePath()).thenReturn(previewPath);
        return v;
    }

    // ── 직접 지원 형식 ─────────────────────────────────────────

    @Test
    void null_버전이면_none_반환() {
        assertThat(resolver.resolve(null)).isEqualTo("none");
    }

    @Test
    void PDF_파일은_pdf_반환() {
        assertThat(resolver.resolve(versionWithName("doc.pdf"))).isEqualTo("pdf");
    }

    @Test
    void JPG_파일은_image_반환() {
        assertThat(resolver.resolve(versionWithName("photo.jpg"))).isEqualTo("image");
    }

    @Test
    void JPEG_파일은_image_반환() {
        assertThat(resolver.resolve(versionWithName("photo.jpeg"))).isEqualTo("image");
    }

    @Test
    void PNG_파일은_image_반환() {
        assertThat(resolver.resolve(versionWithName("image.png"))).isEqualTo("image");
    }

    // ── 오피스 형식 — previewStoragePath 있을 때만 pdf ────────

    @Test
    void DOCX_previewPath_없으면_none_반환() {
        assertThat(resolver.resolve(versionWithNameAndPreview("doc.docx", null))).isEqualTo("none");
    }

    @Test
    void DOCX_previewPath_있으면_pdf_반환() {
        assertThat(resolver.resolve(versionWithNameAndPreview("doc.docx", "previews/doc.pdf"))).isEqualTo("pdf");
    }

    @Test
    void HWPX_previewPath_없으면_none_반환() {
        assertThat(resolver.resolve(versionWithNameAndPreview("doc.hwpx", null))).isEqualTo("none");
    }

    @Test
    void HWPX_previewPath_있으면_pdf_반환() {
        assertThat(resolver.resolve(versionWithNameAndPreview("doc.hwpx", "previews/doc.pdf"))).isEqualTo("pdf");
    }

    @Test
    void PPTX_previewPath_없으면_none_반환() {
        assertThat(resolver.resolve(versionWithNameAndPreview("slide.pptx", null))).isEqualTo("none");
    }

    @Test
    void PPTX_previewPath_있으면_pdf_반환() {
        assertThat(resolver.resolve(versionWithNameAndPreview("slide.pptx", "previews/slide.pdf"))).isEqualTo("pdf");
    }

    @Test
    void PPT_previewPath_있으면_pdf_반환() {
        assertThat(resolver.resolve(versionWithNameAndPreview("slide.ppt", "previews/slide.pdf"))).isEqualTo("pdf");
    }

    @Test
    void XLSX_previewPath_없으면_none_반환() {
        assertThat(resolver.resolve(versionWithNameAndPreview("sheet.xlsx", null))).isEqualTo("none");
    }

    @Test
    void XLSX_previewPath_있으면_pdf_반환() {
        assertThat(resolver.resolve(versionWithNameAndPreview("sheet.xlsx", "previews/sheet.pdf"))).isEqualTo("pdf");
    }

    @Test
    void XLS_previewPath_있으면_pdf_반환() {
        assertThat(resolver.resolve(versionWithNameAndPreview("sheet.xls", "previews/sheet.pdf"))).isEqualTo("pdf");
    }

    // ── 미지원 형식 — 항상 none ────────────────────────────────

    @Test
    void HWP_는_미지원으로_none_반환() {
        // hwp는 LibreOffice 변환 불안정으로 미지원
        assertThat(resolver.resolve(versionWithNameAndPreview("doc.hwp", "previews/doc.pdf"))).isEqualTo("none");
        assertThat(resolver.resolve(versionWithNameAndPreview("doc.hwp", null))).isEqualTo("none");
    }

    @Test
    void ZIP_는_미지원으로_none_반환() {
        assertThat(resolver.resolve(versionWithName("archive.zip"))).isEqualTo("none");
    }

    @Test
    void 확장자_없는_파일은_none_반환() {
        assertThat(resolver.resolve(versionWithName("noext"))).isEqualTo("none");
    }
}
