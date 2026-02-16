package com.zhilian.zr.policy.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PolicyUploadResponse {
    
    private Long policyId;
    
    private String uploadUrl;
    
    private String ossKey;
    
    private String title;
    
    private LocalDateTime createdAt;
}
