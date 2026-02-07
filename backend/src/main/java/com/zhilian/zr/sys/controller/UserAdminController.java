package com.zhilian.zr.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.common.api.PageResponse;
import com.zhilian.zr.common.ids.IdGenerator;
import com.zhilian.zr.sys.entity.UserEntity;
import com.zhilian.zr.sys.mapper.UserDataScopeRelMapper;
import com.zhilian.zr.sys.mapper.UserMapper;
import com.zhilian.zr.sys.mapper.UserRoleMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserAdminController {
  private final UserMapper userMapper;
  private final UserRoleMapper userRoleMapper;
  private final UserDataScopeRelMapper userDataScopeRelMapper;
  private final PasswordEncoder passwordEncoder;

  public UserAdminController(UserMapper userMapper, UserRoleMapper userRoleMapper, UserDataScopeRelMapper userDataScopeRelMapper,
      PasswordEncoder passwordEncoder) {
    this.userMapper = userMapper;
    this.userRoleMapper = userRoleMapper;
    this.userDataScopeRelMapper = userDataScopeRelMapper;
    this.passwordEncoder = passwordEncoder;
  }

  public record SearchRequest(String usernameLike, Integer status, @Min(1) int pageNo, @Min(1) @Max(200) int pageSize) {
  }

  @PostMapping("/search")
  @PreAuthorize("hasAuthority('sys:user:search')")
  public ApiResponse<PageResponse<UserEntity>> search(@RequestBody SearchRequest req) {
    // TODO: implement paging via MyBatis-Plus Page
    List<UserEntity> items = userMapper.selectList(new LambdaQueryWrapper<UserEntity>()
        .like(req.usernameLike() != null && !req.usernameLike().isBlank(), UserEntity::getUsername, req.usernameLike())
        .eq(req.status() != null, UserEntity::getStatus, req.status())
        .orderByAsc(UserEntity::getUsername));
    return ApiResponse.ok(new PageResponse<>(items.size(), req.pageNo(), req.pageSize(), items));
  }

  public record CreateRequest(
      @NotBlank String username,
      @NotBlank String displayName,
      String phone,
      long roleId,
      List<Long> districtIds
  ) {
  }

  @PostMapping
  @PreAuthorize("hasAuthority('sys:user:create')")
  public ApiResponse<Map<String, Object>> create(@RequestBody CreateRequest req) {
    UserEntity existing = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, req.username()));
    if (existing != null) {
      throw new IllegalArgumentException("Username already exists");
    }
    String tempPassword = "Temp@" + Long.toString(System.currentTimeMillis(), 36);

    UserEntity u = new UserEntity();
    u.setUserId(IdGenerator.nextId());
    u.setUsername(req.username());
    u.setDisplayName(req.displayName());
    u.setPhone(req.phone());
    u.setStatus(1);
    u.setPasswordHash(passwordEncoder.encode(tempPassword));
    u.setFailedCount(0);
    u.setCreatedAt(Instant.now());
    u.setUpdatedAt(Instant.now());
    userMapper.insert(u);

    userRoleMapper.deleteByUserId(u.getUserId());
    userRoleMapper.insert(u.getUserId(), req.roleId());

    userDataScopeRelMapper.deleteByUserId(u.getUserId());
    if (req.districtIds() != null) {
      for (Long d : req.districtIds()) {
        userDataScopeRelMapper.insert(u.getUserId(), d);
      }
    }

    return ApiResponse.ok(Map.of(
        "userId", u.getUserId(),
        "tempPassword", tempPassword
    ));
  }

  public record UpdateRequest(String displayName, String phone, Integer status) {
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('sys:user:update')")
  public ApiResponse<Void> update(@PathVariable long id, @RequestBody UpdateRequest req) {
    UserEntity u = userMapper.selectById(id);
    if (u == null) {
      throw new IllegalArgumentException("User not found");
    }
    if (req.displayName() != null) {
      u.setDisplayName(req.displayName());
    }
    if (req.phone() != null) {
      u.setPhone(req.phone());
    }
    if (req.status() != null) {
      u.setStatus(req.status());
    }
    u.setUpdatedAt(Instant.now());
    userMapper.updateById(u);
    return ApiResponse.ok(null);
  }

  @PutMapping("/{id}/disable")
  @PreAuthorize("hasAuthority('sys:user:disable')")
  public ApiResponse<Void> disable(@PathVariable long id) {
    UserEntity u = userMapper.selectById(id);
    if (u == null) {
      throw new IllegalArgumentException("User not found");
    }
    u.setStatus(0);
    u.setUpdatedAt(Instant.now());
    userMapper.updateById(u);
    return ApiResponse.ok(null);
  }

  @PutMapping("/{id}/reset-password")
  @PreAuthorize("hasAuthority('sys:user:resetpwd')")
  public ApiResponse<Map<String, Object>> resetPassword(@PathVariable long id) {
    UserEntity u = userMapper.selectById(id);
    if (u == null) {
      throw new IllegalArgumentException("User not found");
    }
    String tempPassword = "Temp@" + Long.toString(System.currentTimeMillis(), 36);
    u.setPasswordHash(passwordEncoder.encode(tempPassword));
    u.setPwdChangedAt(null);
    u.setUpdatedAt(Instant.now());
    userMapper.updateById(u);
    return ApiResponse.ok(Map.of("tempPassword", tempPassword));
  }

  public record DataScopeRequest(List<Long> districtIds) {
  }

  @PutMapping("/{id}/data-scope")
  @PreAuthorize("hasAuthority('sys:datascope:update')")
  public ApiResponse<Void> updateDataScope(@PathVariable long id, @RequestBody DataScopeRequest req) {
    userDataScopeRelMapper.deleteByUserId(id);
    if (req.districtIds() != null) {
      for (Long d : req.districtIds()) {
        userDataScopeRelMapper.insert(id, d);
      }
    }
    return ApiResponse.ok(null);
  }
}
