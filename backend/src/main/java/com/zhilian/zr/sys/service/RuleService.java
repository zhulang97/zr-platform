package com.zhilian.zr.sys.service;

import com.zhilian.zr.sys.entity.RuleEntity;

import java.util.List;

public interface RuleService {
    RuleEntity createRule(String ruleCode, String name, String description, String ruleType, 
                          String ruleCondition, String params, Integer severity, Integer priority, 
                          Integer enabled, String cronExpr);
    RuleEntity updateRule(Long ruleId, String name, String description, String ruleCondition,
                          String params, Integer severity, Integer priority, Integer enabled, String cronExpr);
    void deleteRule(Long ruleId);
    RuleEntity getRuleById(Long ruleId);
    List<RuleEntity> listRules(Integer enabled);
    void enableRule(Long ruleId);
    void disableRule(Long ruleId);
    void updateLastExecutedAt(Long ruleId);
}
