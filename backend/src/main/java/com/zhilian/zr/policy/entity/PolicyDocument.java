package com.zhilian.zr.policy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_policy_document")
public class PolicyDocument {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long policyId;
    
    private Long userId;
    
    private String title;
    
    private String ossUrl;
    
    private String ossKey;
    
    private String fileName;
    
    private Long fileSize;
    
    private String fileType;
    
    private Integer contentLength;
    
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private String content;
}
