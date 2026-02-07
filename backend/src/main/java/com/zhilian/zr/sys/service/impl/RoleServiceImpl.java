package com.zhilian.zr.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zr.common.ids.IdGenerator;
import com.zhilian.zr.sys.entity.RoleEntity;
import com.zhilian.zr.sys.mapper.RoleMapper;
import com.zhilian.zr.sys.mapper.RolePermissionMapper;
import com.zhilian.zr.sys.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public RoleServiceImpl(RoleMapper roleMapper, RolePermissionMapper rolePermissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    @Transactional
    public RoleEntity createRole(String roleCode, String name, Integer status) {
        RoleEntity existing = roleMapper.selectOne(new LambdaQueryWrapper<RoleEntity>()
            .eq(RoleEntity::getRoleCode, roleCode));
        if (existing != null) {
            throw new IllegalArgumentException("Role code already exists");
        }

        RoleEntity role = new RoleEntity();
        role.setRoleId(IdGenerator.nextId());
        role.setRoleCode(roleCode);
        role.setName(name);
        role.setStatus(status != null ? status : 1);
        role.setCreatedAt(Instant.now());
        roleMapper.insert(role);
        return role;
    }

    @Override
    @Transactional
    public RoleEntity updateRole(Long roleId, String name, Integer status) {
        RoleEntity role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("Role not found");
        }
        if (name != null) {
            role.setName(name);
        }
        if (status != null) {
            role.setStatus(status);
        }
        roleMapper.updateById(role);
        return role;
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        RoleEntity role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("Role not found");
        }
        rolePermissionMapper.deleteByRoleId(roleId);
        roleMapper.deleteById(roleId);
    }

    @Override
    public RoleEntity getRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    @Override
    public List<RoleEntity> listRoles(Integer status) {
        return roleMapper.selectList(new LambdaQueryWrapper<RoleEntity>()
            .eq(status != null, RoleEntity::getStatus, status)
            .orderByAsc(RoleEntity::getRoleCode));
    }

    @Override
    @Transactional
    public void grantPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.deleteByRoleId(roleId);
        if (permissionIds != null) {
            for (Long permId : permissionIds) {
                rolePermissionMapper.insert(roleId, permId);
            }
        }
    }

    @Override
    public List<Long> getPermissionIds(Long roleId) {
        return rolePermissionMapper.listPermissionIdsByRoleId(roleId);
    }

    @Override
    public List<RoleEntity> listByUserId(long userId) {
        return roleMapper.listByUserId(userId);
    }
}
