package com.zhilian.zr.policy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOssProperties {
    
    private String endpoint;
    
    private String accessKeyId;
    
    private String accessKeySecret;
    
    private String bucketName;
    
    private Long policyExpiration = 3600L;
    
    private String callbackUrl;
}
