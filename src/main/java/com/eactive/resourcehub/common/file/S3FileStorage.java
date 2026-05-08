package com.eactive.resourcehub.common.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class S3FileStorage implements FileStorage {

    private final S3Client s3;
    private final String bucket;

    public S3FileStorage(S3Client s3, String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    @Override
    public String store(MultipartFile file, String subPath, String storedFileName) throws IOException {
        String key = subPath + "/" + storedFileName;
        s3.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build(),
            RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );
        log.debug("파일 저장 (R2): {}", key);
        return key;
    }

    @Override
    public InputStream load(String storagePath) throws IOException {
        return s3.getObject(GetObjectRequest.builder()
            .bucket(bucket)
            .key(storagePath)
            .build());
    }

    @Override
    public void delete(String storagePath) throws IOException {
        s3.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(storagePath)
            .build());
        log.debug("파일 삭제 (R2): {}", storagePath);
    }
}
