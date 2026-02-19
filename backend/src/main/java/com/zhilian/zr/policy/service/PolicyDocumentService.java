package com.zhilian.zr.policy.service;

import com.zhilian.zr.policy.config.StorageProperties;
import com.zhilian.zr.policy.dto.PolicyUploadResponse;
import com.zhilian.zr.policy.mapper.PolicyMapper;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyDocumentService {
    
    private final MinioClient minioClient;
    private final StorageProperties storageProperties;
    private final PolicyMapper policyMapper;
    
    /**
     * 直接上传文件到MinIO
     */
    public void uploadFile(String objectKey, byte[] fileBytes, String contentType) throws Exception {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(storageProperties.getBucketName())
                .object(objectKey)
                .stream(new ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                .contentType(contentType != null ? contentType : "application/octet-stream")
                .build()
        );
    }
    
    /**
     * 生成上传信息
     */
    public PolicyUploadResponse generateUploadUrl(Long userId, String fileName, String fileType) {
        // 生成唯一的对象Key
        String policyId = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = getFileExtension(fileName);
        String objectKey = String.format("policies/%d/%s/%s.%s", userId, policyId, timestamp, extension);
        
        PolicyUploadResponse response = new PolicyUploadResponse();
        response.setOssKey(objectKey);
        
        return response;
    }
    
    /**
     * 获取文件访问URL
     */
    public String getFileUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(storageProperties.getBucketName())
                    .object(objectKey)
                    .expiry(7, TimeUnit.DAYS)
                    .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", objectKey, e);
            throw new RuntimeException("生成文件URL失败", e);
        }
    }
    
    /**
     * 删除文件
     */
    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(storageProperties.getBucketName())
                    .object(objectKey)
                    .build()
            );
        } catch (Exception e) {
            log.error("Failed to delete file: {}", objectKey, e);
            throw new RuntimeException("删除文件失败", e);
        }
    }
    
    /**
     * 从MinIO下载文件内容
     */
    public byte[] downloadFile(String objectKey) {
        try (var stream = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                    .bucket(storageProperties.getBucketName())
                    .object(objectKey)
                    .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            log.error("Failed to download file from MinIO: {}", objectKey, e);
            throw new RuntimeException("下载文件失败", e);
        }
    }
    
    /**
     * 获取文件信息
     */
    public FileInfo getFileInfo(String objectKey) {
        try {
            var stat = minioClient.statObject(
                io.minio.StatObjectArgs.builder()
                    .bucket(storageProperties.getBucketName())
                    .object(objectKey)
                    .build()
            );
            return new FileInfo(stat.size(), stat.contentType());
        } catch (Exception e) {
            log.error("Failed to get file info: {}", objectKey, e);
            throw new RuntimeException("获取文件信息失败", e);
        }
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "bin";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    public record FileInfo(long size, String contentType) {}
}
