package com.zhilian.zr.sys.service;

import com.zhilian.zr.sys.entity.RoleEntity;

import java.util.List;

public interface RoleService {
    RoleEntity createRole(String roleCode, String name, Integer status);
    RoleEntity updateRole(Long roleId, String name, Integer status);
    void deleteRole(Long roleId);
    RoleEntity getRoleById(Long roleId);
    List<RoleEntity> listRoles(Integer status);
    void grantPermissions(Long roleId, List<Long> permissionIds);
    List<Long> getPermissionIds(Long roleId);
    List<RoleEntity> listByUserId(long userId);
}
