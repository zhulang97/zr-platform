package com.zhilian.zr.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.common.ids.IdGenerator;
import com.zhilian.zr.sys.entity.PermissionEntity;
import com.zhilian.zr.sys.mapper.PermissionMapper;
import com.zhilian.zr.sys.mapper.RolePermissionMapper;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public PermissionController(PermissionMapper permissionMapper, RolePermissionMapper rolePermissionMapper) {
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    public record SearchRequest(String permCodeLike, String permType, Integer status) {
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('sys:permission:read')")
    public ApiResponse<List<PermissionEntity>> search(@RequestBody SearchRequest req) {
        LambdaQueryWrapper<PermissionEntity> query = new LambdaQueryWrapper<PermissionEntity>()
            .like(req.permCodeLike() != null && !req.permCodeLike().isBlank(), PermissionEntity::getPermCode, req.permCodeLike())
            .eq(req.permType() != null, PermissionEntity::getPermType, req.permType())
            .eq(req.status() != null, PermissionEntity::getStatus, req.status())
            .orderByAsc(PermissionEntity::getSort)
            .orderByAsc(PermissionEntity::getPermCode);
        return ApiResponse.ok(permissionMapper.selectList(query));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('sys:permission:read')")
    public ApiResponse<List<PermissionEntity>> all() {
        return ApiResponse.ok(permissionMapper.selectList(new LambdaQueryWrapper<PermissionEntity>()
            .orderByAsc(PermissionEntity::getSort)
            .orderByAsc(PermissionEntity::getPermCode)));
    }

    public record CreateRequest(
        @NotBlank String permCode,
        @NotBlank String permType,
        @NotBlank String name,
        String path,
        String method,
        Long parentId,
        Integer sort,
        Integer status
    ) {
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:permission:create')")
    public ApiResponse<PermissionEntity> create(@RequestBody CreateRequest req) {
        PermissionEntity existing = permissionMapper.selectOne(new LambdaQueryWrapper<PermissionEntity>()
            .eq(PermissionEntity::getPermCode, req.permCode()));
        if (existing != null) {
            throw new IllegalArgumentException("Permission code already exists");
        }

        PermissionEntity perm = new PermissionEntity();
        perm.setPermId(IdGenerator.nextId());
        perm.setPermCode(req.permCode());
        perm.setPermType(req.permType());
        perm.setName(req.name());
        perm.setPath(req.path());
        perm.setMethod(req.method());
        perm.setParentId(req.parentId());
        perm.setSort(req.sort() != null ? req.sort() : 0);
        perm.setStatus(req.status() != null ? req.status() : 1);
        permissionMapper.insert(perm);
        return ApiResponse.ok(perm);
    }

    public record UpdateRequest(
        String name,
        String path,
        String method,
        Long parentId,
        Integer sort,
        Integer status
    ) {
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:permission:update')")
    public ApiResponse<PermissionEntity> update(@PathVariable long id, @RequestBody UpdateRequest req) {
        PermissionEntity perm = permissionMapper.selectById(id);
        if (perm == null) {
            throw new IllegalArgumentException("Permission not found");
        }
        if (req.name() != null) {
            perm.setName(req.name());
        }
        if (req.path() != null) {
            perm.setPath(req.path());
        }
        if (req.method() != null) {
            perm.setMethod(req.method());
        }
        if (req.parentId() != null) {
            perm.setParentId(req.parentId());
        }
        if (req.sort() != null) {
            perm.setSort(req.sort());
        }
        if (req.status() != null) {
            perm.setStatus(req.status());
        }
        permissionMapper.updateById(perm);
        return ApiResponse.ok(perm);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:permission:delete')")
    public ApiResponse<Void> delete(@PathVariable long id) {
        PermissionEntity perm = permissionMapper.selectById(id);
        if (perm == null) {
            throw new IllegalArgumentException("Permission not found");
        }
        rolePermissionMapper.deleteByPermissionId(id);
        permissionMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:permission:read')")
    public ApiResponse<PermissionEntity> get(@PathVariable long id) {
        PermissionEntity perm = permissionMapper.selectById(id);
        return ApiResponse.ok(perm);
    }
}
