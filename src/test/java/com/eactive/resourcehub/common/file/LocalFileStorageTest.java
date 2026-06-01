package com.eactive.resourcehub.common.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageTest {

    @TempDir
    Path tempDir;

    private LocalFileStorage storage;

    @BeforeEach
    void setUp() {
        storage = new LocalFileStorage(tempDir.toString());
    }

    // ── store ─────────────────────────────────────────────────

    @Test
    void 정상_파일_저장_후_경로_반환() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "PDF content".getBytes());

        String path = storage.store(file, "2024/01", "uuid-test.pdf");

        assertThat(path).isEqualTo("2024/01/uuid-test.pdf");
        assertThat(Files.exists(tempDir.resolve("2024/01/uuid-test.pdf"))).isTrue();
    }

    @Test
    void 서브디렉터리가_없어도_자동_생성() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.txt", "text/plain", "hello".getBytes());

        storage.store(file, "new/nested/dir", "a.txt");

        assertThat(Files.exists(tempDir.resolve("new/nested/dir/a.txt"))).isTrue();
    }

    // ── load ─────────────────────────────────────────────────

    @Test
    void 저장한_파일을_load로_읽을_수_있음() throws IOException {
        byte[] content = "hello world".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", content);
        storage.store(file, "sub", "hello.txt");

        try (InputStream is = storage.load("sub/hello.txt")) {
            assertThat(is.readAllBytes()).isEqualTo(content);
        }
    }

    @Test
    void load_경로_탈출_시도는_예외() {
        assertThatThrownBy(() -> storage.load("../../../etc/passwd"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("경로 탈출 시도 감지");
    }

    @Test
    void load_이중_점_인코딩_경로_탈출도_차단() {
        assertThatThrownBy(() -> storage.load("sub/../../secret.txt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("경로 탈출 시도 감지");
    }

    @Test
    void load_정상_하위_경로는_통과() throws IOException {
        byte[] content = "data".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "f.bin", "application/octet-stream", content);
        storage.store(file, "a/b", "f.bin");

        try (InputStream is = storage.load("a/b/f.bin")) {
            assertThat(is.readAllBytes()).isEqualTo(content);
        }
    }

    // ── delete ────────────────────────────────────────────────

    @Test
    void 저장한_파일_삭제_성공() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "del.txt", "text/plain", "bye".getBytes());
        storage.store(file, "sub", "del.txt");

        storage.delete("sub/del.txt");

        assertThat(Files.exists(tempDir.resolve("sub/del.txt"))).isFalse();
    }

    @Test
    void delete_경로_탈출_시도는_예외() {
        assertThatThrownBy(() -> storage.delete("../sensitive"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("경로 탈출 시도 감지");
    }

    @Test
    void 존재하지_않는_파일_삭제는_예외_없이_통과() throws IOException {
        // Files.deleteIfExists 이므로 없어도 예외 발생 안 함
        storage.delete("sub/nonexistent.txt");
    }

    // ── listAll ───────────────────────────────────────────────

    @Test
    void baseDir_없으면_빈_리스트_반환() throws IOException {
        LocalFileStorage emptyStorage = new LocalFileStorage(
                tempDir.resolve("nonexistent").toString());
        assertThat(emptyStorage.listAll(java.time.Instant.now())).isEmpty();
    }

    @Test
    void 오래된_파일만_listAll에_포함() throws IOException {
        MockMultipartFile file = new MockMultipartFile("f", "old.txt", "text/plain", "x".getBytes());
        storage.store(file, "sub", "old.txt");

        // 미래 시각으로 조회하면 포함됨
        var result = storage.listAll(java.time.Instant.now().plusSeconds(60));
        assertThat(result).anyMatch(p -> p.endsWith("old.txt"));
    }
}
