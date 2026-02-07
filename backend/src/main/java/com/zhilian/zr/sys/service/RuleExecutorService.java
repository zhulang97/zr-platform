package com.zhilian.zr.sys.service;

import com.zhilian.zr.sys.entity.RuleEntity;

import java.util.List;

public interface RuleExecutorService {
    void executeRule(RuleEntity rule);
    void executeAllRules();
    List<RuleEntity> getActiveRules();
}
