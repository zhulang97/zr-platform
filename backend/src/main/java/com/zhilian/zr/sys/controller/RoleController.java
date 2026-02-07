package com.zhilian.zr.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.sys.entity.RoleEntity;
import com.zhilian.zr.sys.mapper.PermissionMapper;
import com.zhilian.zr.sys.mapper.RoleMapper;
import com.zhilian.zr.sys.service.RoleService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public RoleController(RoleService roleService, RoleMapper roleMapper, PermissionMapper permissionMapper) {
        this.roleService = roleService;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    public record SearchRequest(String roleCodeLike, Integer status) {
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('sys:role:search')")
    public ApiResponse<List<RoleEntity>> search(@RequestBody SearchRequest req) {
        List<RoleEntity> roles = roleMapper.selectList(new LambdaQueryWrapper<RoleEntity>()
            .like(req.roleCodeLike() != null && !req.roleCodeLike().isBlank(), RoleEntity::getRoleCode, req.roleCodeLike())
            .eq(req.status() != null, RoleEntity::getStatus, req.status())
            .orderByAsc(RoleEntity::getRoleCode));
        return ApiResponse.ok(roles);
    }

    public record CreateRequest(
        @NotBlank String roleCode,
        @NotBlank String name,
        Integer status
    ) {
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:role:create')")
    public ApiResponse<RoleEntity> create(@RequestBody CreateRequest req) {
        RoleEntity role = roleService.createRole(req.roleCode(), req.name(), req.status());
        return ApiResponse.ok(role);
    }

    public record UpdateRequest(String name, Integer status) {
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:update')")
    public ApiResponse<RoleEntity> update(@PathVariable long id, @RequestBody UpdateRequest req) {
        RoleEntity role = roleService.updateRole(id, req.name(), req.status());
        return ApiResponse.ok(role);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:delete')")
    public ApiResponse<Void> delete(@PathVariable long id) {
        roleService.deleteRole(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:view')")
    public ApiResponse<RoleEntity> get(@PathVariable long id) {
        RoleEntity role = roleService.getRoleById(id);
        return ApiResponse.ok(role);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('sys:role:view')")
    public ApiResponse<List<RoleEntity>> list(@RequestParam(required = false) Integer status) {
        List<RoleEntity> roles = roleService.listRoles(status);
        return ApiResponse.ok(roles);
    }

    public record GrantPermissionsRequest(List<Long> permissionIds) {
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('sys:role:grant')")
    public ApiResponse<Void> grantPermissions(@PathVariable long id, @RequestBody GrantPermissionsRequest req) {
        roleService.grantPermissions(id, req.permissionIds());
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('sys:role:view')")
    public ApiResponse<List<Map<String, Object>>> getPermissions(@PathVariable long id) {
        List<Long> permIds = roleService.getPermissionIds(id);
        return ApiResponse.ok(permIds.stream().map(permId -> (Map<String, Object>) Map.<String, Object>of(
            "permId", permId
        )).toList());
    }
}
