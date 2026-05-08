package com.eactive.resourcehub.common.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class FileStorageConfig {

    @Bean
    @ConditionalOnProperty(name = "resourcehub.storage.type", havingValue = "s3")
    public FileStorage s3FileStorage(
            @Value("${resourcehub.storage.s3.endpoint}") String endpoint,
            @Value("${resourcehub.storage.s3.access-key}") String accessKey,
            @Value("${resourcehub.storage.s3.secret-key}") String secretKey,
            @Value("${resourcehub.storage.s3.bucket}") String bucket,
            @Value("${resourcehub.storage.s3.region:auto}") String region) {

        S3Client s3 = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();

        return new S3FileStorage(s3, bucket);
    }

    @Bean
    @ConditionalOnMissingBean(FileStorage.class)
    public FileStorage localFileStorage(
            @Value("${resourcehub.upload.base-dir:./storage/uploads}") String baseDir) {
        return new LocalFileStorage(baseDir);
    }
}
