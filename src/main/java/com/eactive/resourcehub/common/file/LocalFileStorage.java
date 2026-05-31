package com.eactive.resourcehub.common.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class LocalFileStorage implements FileStorage {

    private final String baseDir;

    public LocalFileStorage(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public String store(MultipartFile file, String subPath, String storedFileName) throws IOException {
        Path dir = Paths.get(baseDir).resolve(subPath);
        Files.createDirectories(dir);
        Path target = dir.resolve(storedFileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        log.debug("파일 저장: {}", storedFileName);
        return subPath + "/" + storedFileName;
    }

    @Override
    public InputStream load(String storagePath) throws IOException {
        Path target = resolveAndValidate(storagePath);
        return Files.newInputStream(target);
    }

    @Override
    public void delete(String storagePath) throws IOException {
        Path target = resolveAndValidate(storagePath);
        boolean deleted = Files.deleteIfExists(target);
        if (deleted) {
            log.debug("파일 삭제: {}", storagePath);
        }
    }

    private Path resolveAndValidate(String storagePath) {
        Path base = Paths.get(baseDir).normalize();
        Path resolved = base.resolve(storagePath).normalize();
        if (!resolved.startsWith(base)) {
            throw new IllegalArgumentException("경로 탈출 시도 감지: " + storagePath);
        }
        return resolved;
    }

    @Override
    public List<String> listAll(Instant olderThan) throws IOException {
        Path base = Paths.get(baseDir);
        if (!Files.exists(base)) return List.of();
        try (Stream<Path> stream = Files.walk(base)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        try {
                            return Files.getLastModifiedTime(p).toInstant().isBefore(olderThan);
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .map(p -> base.relativize(p).toString().replace('\\', '/'))
                    .toList();
        }
    }
}
