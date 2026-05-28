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

    @Test
    void DOCX_previewPath_없으면_none_반환() {
        DocumentVersion v = versionWithName("doc.docx");
        when(v.getPreviewStoragePath()).thenReturn(null);
        assertThat(resolver.resolve(v)).isEqualTo("none");
    }

    @Test
    void DOCX_previewPath_있으면_pdf_반환() {
        DocumentVersion v = versionWithName("doc.docx");
        when(v.getPreviewStoragePath()).thenReturn("previews/doc.pdf");
        assertThat(resolver.resolve(v)).isEqualTo("pdf");
    }

    @Test
    void HWP_previewPath_없으면_none_반환() {
        DocumentVersion v = versionWithName("document.hwp");
        when(v.getPreviewStoragePath()).thenReturn(null);
        assertThat(resolver.resolve(v)).isEqualTo("none");
    }

    @Test
    void HWPX_previewPath_있으면_pdf_반환() {
        DocumentVersion v = versionWithName("document.hwpx");
        when(v.getPreviewStoragePath()).thenReturn("previews/document.pdf");
        assertThat(resolver.resolve(v)).isEqualTo("pdf");
    }

    @Test
    void 알수없는_확장자는_none_반환() {
        assertThat(resolver.resolve(versionWithName("data.xlsx"))).isEqualTo("none");
        assertThat(resolver.resolve(versionWithName("archive.zip"))).isEqualTo("none");
        assertThat(resolver.resolve(versionWithName("noext"))).isEqualTo("none");
    }
}
