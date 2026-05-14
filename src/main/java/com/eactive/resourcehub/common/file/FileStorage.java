package com.eactive.resourcehub.common.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

public interface FileStorage {

    /**
     * @return storagePath (subPath/storedFileName) relative to base-dir
     */
    String store(MultipartFile file, String subPath, String storedFileName) throws IOException;

    InputStream load(String storagePath) throws IOException;

    void delete(String storagePath) throws IOException;

    /**
     * 스토리지 내 모든 파일 경로를 반환한다.
     * olderThan 이후에 생성된 파일은 제외 (업로드 중인 파일 보호).
     * S3 등 구현체는 기본 빈 목록 반환 — 로컬 스토리지만 완전 지원.
     */
    default List<String> listAll(Instant olderThan) throws IOException {
        return List.of();
    }
}
