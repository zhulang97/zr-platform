package com.zhilian.zr.policy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    
    // 存储类型: minio
    private String type = "minio";
    
    // MinIO配置
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private boolean secure = true;
}
