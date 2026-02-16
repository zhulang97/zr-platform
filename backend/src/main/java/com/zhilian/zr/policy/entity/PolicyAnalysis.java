package com.zhilian.zr.policy.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhilian.zr.policy.dto.PolicyConditions;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_policy_analysis")
public class PolicyAnalysis {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long analysisId;
    
    private Long policyId;
    
    private Integer version;
    
    private String isLatest;
    
    private String conditionsJson;
    
    private String explanation;
    
    private Integer analyzedSegments;
    
    private Integer totalSegments;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private PolicyConditions conditions;
}
