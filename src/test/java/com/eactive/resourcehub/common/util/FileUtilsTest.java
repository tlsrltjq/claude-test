package com.eactive.resourcehub.common.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @Test
    void 소문자_확장자_추출() {
        assertEquals("pdf", FileUtils.extension("report.pdf"));
    }

    @Test
    void 대문자_확장자_소문자로_반환() {
        assertEquals("pdf", FileUtils.extension("report.PDF"));
    }

    @Test
    void 혼합대소문자_확장자() {
        assertEquals("docx", FileUtils.extension("Resume.DOCX"));
    }

    @Test
    void 확장자_없는_파일명() {
        assertEquals("", FileUtils.extension("noextension"));
    }

    @Test
    void null_파일명() {
        assertEquals("", FileUtils.extension(null));
    }

    @Test
    void 점만_있는_파일명() {
        assertEquals("", FileUtils.extension("."));
    }

    @Test
    void 여러_점_포함_파일명() {
        assertEquals("gz", FileUtils.extension("archive.tar.gz"));
    }

    @Test
    void 허용_확장자_포함() {
        Set<String> allowed = Set.of("pdf", "jpg", "png", "docx");
        assertTrue(FileUtils.isAllowedExtension("file.pdf", allowed));
        assertTrue(FileUtils.isAllowedExtension("photo.PNG", allowed));
    }

    @Test
    void 비허용_확장자_거부() {
        Set<String> allowed = Set.of("pdf", "jpg", "png");
        assertFalse(FileUtils.isAllowedExtension("virus.exe", allowed));
        assertFalse(FileUtils.isAllowedExtension("script.js", allowed));
    }
}
