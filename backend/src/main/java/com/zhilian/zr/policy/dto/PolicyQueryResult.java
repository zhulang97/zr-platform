package com.zhilian.zr.policy.dto;

import lombok.Data;
import java.util.List;
import com.zhilian.zr.person.dto.PersonDtos;

@Data
public class PolicyQueryResult {
    
    private Long policyId;
    
    private String policyTitle;
    
    private Integer version;
    
    private PolicyConditions conditions;
    
    private Long total;
    
    private List<PersonDtos.PersonRow> items;
}
