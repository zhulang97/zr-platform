package com.zhilian.zr.policy.service;

import com.aliyun.oss.OSS;
import com.zhilian.zr.policy.config.AliyunOssProperties;
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
    
    private final OSS ossClient;
    private final AliyunOssProperties ossProperties;
    
    /**
     * 从OSS文件提取文本内容
     */
    public String extractText(String ossKey) {
        try {
            // 从OSS下载文件
            byte[] fileBytes = ossClient.getObject(ossProperties.getBucketName(), ossKey).getObjectContent().readAllBytes();
            
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
            log.error("Failed to extract text from file: {}", ossKey, e);
            throw new RuntimeException("文件文本提取失败", e);
        }
    }
    
    /**
     * 获取文件基本信息
     */
    public FileInfo getFileInfo(String ossKey) {
        try {
            var object = ossClient.getObject(ossProperties.getBucketName(), ossKey);
            var metadata = object.getObjectMetadata();
            
            FileInfo info = new FileInfo();
            info.setContentType(metadata.getContentType());
            info.setSize(metadata.getContentLength());
            info.setLastModified(metadata.getLastModified());
            
            return info;
        } catch (Exception e) {
            log.error("Failed to get file info: {}", ossKey, e);
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
