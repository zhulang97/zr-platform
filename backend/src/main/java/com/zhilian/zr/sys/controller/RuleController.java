package com.zhilian.zr.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.sys.entity.RuleEntity;
import com.zhilian.zr.sys.mapper.RuleMapper;
import com.zhilian.zr.sys.service.RuleService;
import com.zhilian.zr.sys.service.RuleExecutorService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final RuleService ruleService;
    private final RuleMapper ruleMapper;
    private final RuleExecutorService ruleExecutorService;

    public RuleController(RuleService ruleService, RuleMapper ruleMapper, RuleExecutorService ruleExecutorService) {
        this.ruleService = ruleService;
        this.ruleMapper = ruleMapper;
        this.ruleExecutorService = ruleExecutorService;
    }

    public record SearchRequest(String ruleCodeLike, String ruleType, Integer severity, Integer enabled) {
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('sys:rule:search')")
    public ApiResponse<List<RuleEntity>> search(@RequestBody SearchRequest req) {
        List<RuleEntity> rules = ruleMapper.selectList(new LambdaQueryWrapper<RuleEntity>()
            .like(req.ruleCodeLike() != null && !req.ruleCodeLike().isBlank(), RuleEntity::getRuleCode, req.ruleCodeLike())
            .eq(req.ruleType() != null, RuleEntity::getRuleType, req.ruleType())
            .eq(req.severity() != null, RuleEntity::getSeverity, req.severity())
            .eq(req.enabled() != null, RuleEntity::getEnabled, req.enabled())
            .orderByAsc(RuleEntity::getPriority)
            .orderByAsc(RuleEntity::getRuleCode));
        return ApiResponse.ok(rules);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('sys:rule:view')")
    public ApiResponse<List<RuleEntity>> list(@RequestParam(required = false) Integer enabled) {
        List<RuleEntity> rules = ruleService.listRules(enabled);
        return ApiResponse.ok(rules);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:rule:view')")
    public ApiResponse<RuleEntity> get(@PathVariable long id) {
        RuleEntity rule = ruleService.getRuleById(id);
        return ApiResponse.ok(rule);
    }

    public record CreateRequest(
        @NotBlank String ruleCode,
        @NotBlank String name,
        String description,
        String ruleType,
        String ruleCondition,
        String params,
        Integer severity,
        Integer priority,
        Integer enabled,
        String cronExpr
    ) {
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:rule:create')")
    public ApiResponse<RuleEntity> create(@RequestBody CreateRequest req) {
        RuleEntity rule = ruleService.createRule(req.ruleCode(), req.name(), req.description(), req.ruleType(),
            req.ruleCondition(), req.params(), req.severity(), req.priority(), req.enabled(), req.cronExpr());
        return ApiResponse.ok(rule);
    }

    public record UpdateRequest(
        String name,
        String description,
        String ruleCondition,
        String params,
        Integer severity,
        Integer priority,
        Integer enabled,
        String cronExpr
    ) {
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:rule:update')")
    public ApiResponse<RuleEntity> update(@PathVariable long id, @RequestBody UpdateRequest req) {
        RuleEntity rule = ruleService.updateRule(id, req.name(), req.description(), req.ruleCondition(),
            req.params(), req.severity(), req.priority(), req.enabled(), req.cronExpr());
        return ApiResponse.ok(rule);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:rule:delete')")
    public ApiResponse<Void> delete(@PathVariable long id) {
        ruleService.deleteRule(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('sys:rule:update')")
    public ApiResponse<Void> enable(@PathVariable long id) {
        ruleService.enableRule(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('sys:rule:update')")
    public ApiResponse<Void> disable(@PathVariable long id) {
        ruleService.disableRule(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/execute")
    @PreAuthorize("hasAuthority('sys:rule:execute')")
    public ApiResponse<Void> execute(@PathVariable long id) {
        RuleEntity rule = ruleService.getRuleById(id);
        if (rule == null) {
            throw new IllegalArgumentException("Rule not found");
        }
        ruleExecutorService.executeRule(rule);
        return ApiResponse.ok(null);
    }

    @PostMapping("/execute-all")
    @PreAuthorize("hasAuthority('sys:rule:execute')")
    public ApiResponse<Void> executeAll() {
        ruleExecutorService.executeAllRules();
        return ApiResponse.ok(null);
    }
}
