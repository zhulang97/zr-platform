package com.zhilian.zr.policy.dto;

import lombok.Data;

@Data
public class PolicyUploadRequest {
    
    private String title;
    
    private String fileName;
    
    private Long fileSize;
    
    private String fileType;
    
    private String content;
}
