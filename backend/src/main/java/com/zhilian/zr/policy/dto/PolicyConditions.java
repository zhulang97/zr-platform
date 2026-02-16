package com.zhilian.zr.policy.dto;

import lombok.Data;
import java.util.List;

@Data
public class PolicyConditions {
    
    private String title;
    
    // 户籍区县
    private List<Long> districtIds;
    
    // 残疾类别: LIMB, VISION, HEARING, SPEECH, INTELLECTUAL, MENTAL
    private List<String> disabilityCategories;
    
    // 残疾等级: 1, 2, 3, 4
    private List<String> disabilityLevels;
    
    // 残车补贴
    private Boolean hasCar;
    
    // 医疗补贴
    private Boolean hasMedicalSubsidy;
    
    // 养老补贴
    private Boolean hasPensionSubsidy;
    
    // 盲人证
    private Boolean hasBlindCard;
    
    // 最小年龄
    private Integer ageMin;
    
    // 最大年龄
    private Integer ageMax;
    
    // AI解释说明
    private String explanation;
}
