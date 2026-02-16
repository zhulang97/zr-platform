package com.zhilian.zr.policy.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PolicyDocumentVO {
    
    private Long policyId;
    
    private String title;
    
    private String fileName;
    
    private String fileType;
    
    private Long fileSize;
    
    private Integer contentLength;
    
    private String status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // 最新版本信息
    private Integer latestVersion;
    
    private String latestExplanation;
}
