package com.zhilian.zr.policy.service;

import com.zhilian.zr.policy.config.StorageProperties;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileExtractService {
    
    private final MinioClient minioClient;
    private final StorageProperties storageProperties;
    
    /**
     * 从MinIO文件提取文本内容
     */
    public String extractText(String objectKey) {
        try {
            // 从MinIO下载文件
            byte[] fileBytes = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                    .bucket(storageProperties.getBucketName())
                    .object(objectKey)
                    .build()
            ).readAllBytes();
            
            // 使用Tika解析
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1); // 无限制
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            
            try (InputStream stream = new ByteArrayInputStream(fileBytes)) {
                parser.parse(stream, handler, metadata, context);
            }
            
            return handler.toString();
        } catch (Exception e) {
            log.error("Failed to extract text from file: {}", objectKey, e);
            throw new RuntimeException("文件文本提取失败", e);
        }
    }
    
    /**
     * 获取文件基本信息
     */
    public FileInfo getFileInfo(String objectKey) {
        try {
            var stat = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(storageProperties.getBucketName())
                    .object(objectKey)
                    .build()
            );
            
            FileInfo info = new FileInfo();
            info.setContentType(stat.contentType());
            info.setSize(stat.size());
            info.setLastModified(java.util.Date.from(stat.lastModified().toInstant()));
            
            return info;
        } catch (Exception e) {
            log.error("Failed to get file info: {}", objectKey, e);
            throw new RuntimeException("获取文件信息失败", e);
        }
    }
    
    public static class FileInfo {
        private String contentType;
        private Long size;
        private java.util.Date lastModified;
        
        // Getters and Setters
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public Long getSize() { return size; }
        public void setSize(Long size) { this.size = size; }
        public java.util.Date getLastModified() { return lastModified; }
        public void setLastModified(java.util.Date lastModified) { this.lastModified = lastModified; }
    }
}
