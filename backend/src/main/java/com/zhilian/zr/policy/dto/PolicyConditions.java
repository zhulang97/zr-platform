package com.zhilian.zr.policy.dto;

import lombok.Data;
import java.util.List;

@Data
public class PolicyConditions {
    
    public String title;
    
    public List<Long> districtIds;
    
    public List<String> disabilityCategories;
    
    public List<String> disabilityLevels;
    
    public Boolean hasCar;
    
    public Boolean hasMedicalSubsidy;
    
    public Boolean hasPensionSubsidy;
    
    public Boolean hasBlindCard;
    
    public Integer ageMin;
    
    public Integer ageMax;
    
    public String explanation;
}
