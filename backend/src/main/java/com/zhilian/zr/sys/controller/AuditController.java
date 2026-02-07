package com.zhilian.zr.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zr.audit.entity.AuditLogEntity;
import com.zhilian.zr.audit.mapper.AuditLogMapper;
import com.zhilian.zr.common.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogMapper auditLogMapper;

    public AuditController(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public record SearchRequest(
        Long userId,
        String actionLike,
        String resourceLike,
        String resultCode,
        Instant startAt,
        Instant endAt
    ) {
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('audit:read')")
    public ApiResponse<List<AuditLogEntity>> search(@RequestBody SearchRequest req) {
        LambdaQueryWrapper<AuditLogEntity> query = new LambdaQueryWrapper<AuditLogEntity>()
            .eq(req.userId() != null, AuditLogEntity::getUserId, req.userId())
            .like(req.actionLike() != null && !req.actionLike().isBlank(), AuditLogEntity::getAction, req.actionLike())
            .like(req.resourceLike() != null && !req.resourceLike().isBlank(), AuditLogEntity::getResource, req.resourceLike())
            .eq(req.resultCode() != null, AuditLogEntity::getResultCode, req.resultCode())
            .ge(req.startAt() != null, AuditLogEntity::getCreatedAt, req.startAt())
            .le(req.endAt() != null, AuditLogEntity::getCreatedAt, req.endAt())
            .orderByDesc(AuditLogEntity::getCreatedAt);
        return ApiResponse.ok(auditLogMapper.selectList(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('audit:read')")
    public ApiResponse<AuditLogEntity> get(@PathVariable long id) {
        AuditLogEntity log = auditLogMapper.selectById(id);
        return ApiResponse.ok(log);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('audit:read')")
    public ApiResponse<List<AuditLogEntity>> getByUser(@PathVariable long userId) {
        return ApiResponse.ok(auditLogMapper.listByUserId(userId));
    }
}
