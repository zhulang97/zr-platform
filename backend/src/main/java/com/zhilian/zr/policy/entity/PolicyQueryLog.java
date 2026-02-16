package com.zhilian.zr.policy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_policy_query_log")
public class PolicyQueryLog {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;
    
    private Long analysisId;
    
    private String filtersJson;
    
    private Integer totalResults;
    
    private LocalDateTime executedAt;
}
