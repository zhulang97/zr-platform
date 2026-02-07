package com.zhilian.zr.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zr.common.ids.IdGenerator;
import com.zhilian.zr.sys.entity.RuleEntity;
import com.zhilian.zr.sys.mapper.RuleMapper;
import com.zhilian.zr.sys.service.RuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RuleServiceImpl implements RuleService {

    private final RuleMapper ruleMapper;

    public RuleServiceImpl(RuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
    }

    @Override
    @Transactional
    public RuleEntity createRule(String ruleCode, String name, String description, String ruleType,
                                 String ruleCondition, String params, Integer severity, Integer priority,
                                 Integer enabled, String cronExpr) {
        RuleEntity existing = ruleMapper.selectOne(new LambdaQueryWrapper<RuleEntity>()
            .eq(RuleEntity::getRuleCode, ruleCode));
        if (existing != null) {
            throw new IllegalArgumentException("Rule code already exists");
        }

        RuleEntity rule = new RuleEntity();
        rule.setRuleId(IdGenerator.nextId());
        rule.setRuleCode(ruleCode);
        rule.setName(name);
        rule.setDescription(description);
        rule.setRuleType(ruleType);
        rule.setRuleCondition(ruleCondition);
        rule.setParams(params);
        rule.setSeverity(severity != null ? severity : 1);
        rule.setPriority(priority != null ? priority : 5);
        rule.setEnabled(enabled != null ? enabled : 1);
        rule.setCronExpr(cronExpr);
        rule.setCreatedAt(Instant.now());
        rule.setUpdatedAt(Instant.now());
        ruleMapper.insert(rule);
        return rule;
    }

    @Override
    @Transactional
    public RuleEntity updateRule(Long ruleId, String name, String description, String ruleCondition,
                                 String params, Integer severity, Integer priority, Integer enabled, String cronExpr) {
        RuleEntity rule = ruleMapper.selectById(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("Rule not found");
        }
        if (name != null) {
            rule.setName(name);
        }
        if (description != null) {
            rule.setDescription(description);
        }
        if (ruleCondition != null) {
            rule.setRuleCondition(ruleCondition);
        }
        if (params != null) {
            rule.setParams(params);
        }
        if (severity != null) {
            rule.setSeverity(severity);
        }
        if (priority != null) {
            rule.setPriority(priority);
        }
        if (enabled != null) {
            rule.setEnabled(enabled);
        }
        if (cronExpr != null) {
            rule.setCronExpr(cronExpr);
        }
        rule.setUpdatedAt(Instant.now());
        ruleMapper.updateById(rule);
        return rule;
    }

    @Override
    @Transactional
    public void deleteRule(Long ruleId) {
        RuleEntity rule = ruleMapper.selectById(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("Rule not found");
        }
        ruleMapper.deleteById(ruleId);
    }

    @Override
    public RuleEntity getRuleById(Long ruleId) {
        return ruleMapper.selectById(ruleId);
    }

    @Override
    public List<RuleEntity> listRules(Integer enabled) {
        return ruleMapper.selectList(new LambdaQueryWrapper<RuleEntity>()
            .eq(enabled != null, RuleEntity::getEnabled, enabled)
            .orderByAsc(RuleEntity::getPriority)
            .orderByAsc(RuleEntity::getRuleCode));
    }

    @Override
    @Transactional
    public void enableRule(Long ruleId) {
        RuleEntity rule = ruleMapper.selectById(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("Rule not found");
        }
        rule.setEnabled(1);
        rule.setUpdatedAt(Instant.now());
        ruleMapper.updateById(rule);
    }

    @Override
    @Transactional
    public void disableRule(Long ruleId) {
        RuleEntity rule = ruleMapper.selectById(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("Rule not found");
        }
        rule.setEnabled(0);
        rule.setUpdatedAt(Instant.now());
        ruleMapper.updateById(rule);
    }

    @Override
    @Transactional
    public void updateLastExecutedAt(Long ruleId) {
        RuleEntity rule = ruleMapper.selectById(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("Rule not found");
        }
        rule.setLastExecutedAt(Instant.now());
        rule.setUpdatedAt(Instant.now());
        ruleMapper.updateById(rule);
    }
}
