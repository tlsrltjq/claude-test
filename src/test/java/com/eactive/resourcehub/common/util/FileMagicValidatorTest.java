package com.eactive.resourcehub.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class FileMagicValidatorTest {

    // ── 정상 시그니처 ───────────────────────────────────────────

    @Test
    void PDF_정상_시그니처() throws Exception {
        byte[] bytes = buildBytes(new byte[]{0x25, 0x50, 0x44, 0x46}, 16);
        assertTrue(FileMagicValidator.validate(mockFile(bytes), "pdf"));
    }

    @Test
    void JPG_정상_시그니처() throws Exception {
        byte[] bytes = buildBytes(new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF}, 16);
        assertTrue(FileMagicValidator.validate(mockFile(bytes), "jpg"));
        assertTrue(FileMagicValidator.validate(mockFile(bytes), "jpeg"));
    }

    @Test
    void PNG_정상_시그니처() throws Exception {
        byte[] bytes = buildBytes(new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}, 16);
        assertTrue(FileMagicValidator.validate(mockFile(bytes), "png"));
    }

    @Test
    void DOCX_정상_시그니처_ZIP기반() throws Exception {
        byte[] bytes = buildBytes(new byte[]{0x50, 0x4B, 0x03, 0x04}, 16);
        assertTrue(FileMagicValidator.validate(mockFile(bytes), "docx"));
        assertTrue(FileMagicValidator.validate(mockFile(bytes), "pptx"));
        assertTrue(FileMagicValidator.validate(mockFile(bytes), "hwpx"));
    }

    @Test
    void HWP_OLE2_정상_시그니처() throws Exception {
        byte[] bytes = buildBytes(new byte[]{(byte)0xD0, (byte)0xCF, 0x11, (byte)0xE0,
                (byte)0xA1, (byte)0xB1, 0x1A, (byte)0xE1}, 16);
        assertTrue(FileMagicValidator.validate(mockFile(bytes), "hwp"));
        assertTrue(FileMagicValidator.validate(mockFile(bytes), "ppt"));
    }

    @Test
    void HWP_레거시_시그니처() throws Exception {
        // "HWP Doc"
        byte[] bytes = buildBytes(new byte[]{0x48, 0x57, 0x50, 0x20, 0x44, 0x6F, 0x63}, 16);
        assertTrue(FileMagicValidator.validate(mockFile(bytes), "hwp"));
    }

    // ── 시그니처 불일치 ────────────────────────────────────────

    @Test
    void PDF_확장자에_JPG_시그니처는_거부() throws Exception {
        byte[] jpgBytes = buildBytes(new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF}, 16);
        assertFalse(FileMagicValidator.validate(mockFile(jpgBytes), "pdf"));
    }

    @Test
    void PNG_확장자에_PDF_시그니처는_거부() throws Exception {
        byte[] pdfBytes = buildBytes(new byte[]{0x25, 0x50, 0x44, 0x46}, 16);
        assertFalse(FileMagicValidator.validate(mockFile(pdfBytes), "png"));
    }

    @Test
    void 알수없는_확장자는_거부() throws Exception {
        byte[] bytes = buildBytes(new byte[]{0x25, 0x50, 0x44, 0x46}, 16);
        assertFalse(FileMagicValidator.validate(mockFile(bytes), "exe"));
        assertFalse(FileMagicValidator.validate(mockFile(bytes), "txt"));
        assertFalse(FileMagicValidator.validate(mockFile(bytes), "js"));
    }

    @Test
    void 파일이_4바이트_미만이면_거부() throws Exception {
        byte[] tooShort = {0x25, 0x50, 0x44};
        assertFalse(FileMagicValidator.validate(mockFile(tooShort), "pdf"));
    }

    @Test
    void 빈_파일은_거부() throws Exception {
        assertFalse(FileMagicValidator.validate(mockFile(new byte[0]), "pdf"));
    }

    @Test
    void 대소문자_구분없이_확장자_처리() throws Exception {
        byte[] bytes = buildBytes(new byte[]{0x25, 0x50, 0x44, 0x46}, 16);
        assertTrue(FileMagicValidator.validate(mockFile(bytes), "PDF"));
        assertTrue(FileMagicValidator.validate(mockFile(bytes), "Pdf"));
    }

    // ── 헬퍼 ────────────────────────────────────────────────────

    private MockMultipartFile mockFile(byte[] content) {
        return new MockMultipartFile("file", "test.bin", "application/octet-stream", content);
    }

    private byte[] buildBytes(byte[] header, int totalLength) {
        byte[] result = new byte[Math.max(header.length, totalLength)];
        System.arraycopy(header, 0, result, 0, header.length);
        return result;
    }
}
