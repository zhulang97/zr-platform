package com.zhilian.zr.policy.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Slf4j
@Configuration
public class MinioConfig {

    private final StorageProperties properties;
    private MinioClient minioClient;

    public MinioConfig(StorageProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MinioClient minioClient() {
        this.minioClient = MinioClient.builder()
            .endpoint(properties.getEndpoint())
            .credentials(properties.getAccessKey(), properties.getSecretKey())
            .build();
        return this.minioClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initBucket() {
        try {
            String bucketName = properties.getBucketName();
            
            boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );

            if (!found) {
                log.info("Bucket '{}' does not exist, creating it...", bucketName);
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                );
                log.info("Bucket '{}' created successfully", bucketName);
            } else {
                log.info("Bucket '{}' already exists", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket", e);
        }
    }
}
