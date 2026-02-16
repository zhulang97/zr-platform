package com.zhilian.zr.policy.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PolicyQueryRequest {
    
    private Long analysisId;
    
    private PolicyConditions conditions;
    
    private Integer pageNo;
    
    private Integer pageSize;
}
