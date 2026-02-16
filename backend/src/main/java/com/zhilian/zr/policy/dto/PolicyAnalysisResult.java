package com.zhilian.zr.policy.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PolicyAnalysisResult {
    
    private Long analysisId;
    
    private Integer version;
    
    private Boolean isLatest;
    
    private PolicyConditions conditions;
    
    private String explanation;
    
    private Integer analyzedSegments;
    
    private Integer totalSegments;
    
    private LocalDateTime createdAt;
    
    // 版本对比相关
    private List<ConditionDiff> diffs;
}
