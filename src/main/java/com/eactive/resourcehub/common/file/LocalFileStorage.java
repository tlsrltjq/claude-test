package com.eactive.resourcehub.common.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
public class LocalFileStorage implements FileStorage {

    @Value("${resourcehub.upload.base-dir:./storage/uploads}")
    private String baseDir;

    @Override
    public String store(MultipartFile file, String subPath, String storedFileName) throws IOException {
        Path dir = Paths.get(baseDir).resolve(subPath);
        Files.createDirectories(dir);
        Path target = dir.resolve(storedFileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        log.debug("파일 저장: {}", target.toAbsolutePath());
        return subPath + "/" + storedFileName;
    }

    @Override
    public InputStream load(String storagePath) throws IOException {
        return Files.newInputStream(Paths.get(baseDir).resolve(storagePath));
    }

    @Override
    public void delete(String storagePath) throws IOException {
        boolean deleted = Files.deleteIfExists(Paths.get(baseDir).resolve(storagePath));
        if (deleted) {
            log.debug("파일 삭제: {}", storagePath);
        }
    }
}
