package com.zhilian.zr.policy.dto;

import lombok.Data;

@Data
public class ConditionDiff {
    
    private String field;
    
    private String fieldName;
    
    private Object oldValue;
    
    private Object newValue;
    
    private String diffType; // ADDED, REMOVED, MODIFIED
}
