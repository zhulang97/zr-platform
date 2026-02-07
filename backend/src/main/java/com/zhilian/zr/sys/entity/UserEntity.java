package com.zhilian.zr.sys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("t_user")
public class UserEntity {
  @TableId
  private Long userId;
  private String username;
  private String passwordHash;
  private String displayName;
  private String phone;
  private Integer status;
  private Instant pwdChangedAt;
  private Instant lastLoginAt;
  private Integer failedCount;
  private Instant lockedUntil;
  private Instant createdAt;
  private Instant updatedAt;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Instant getPwdChangedAt() {
    return pwdChangedAt;
  }

  public void setPwdChangedAt(Instant pwdChangedAt) {
    this.pwdChangedAt = pwdChangedAt;
  }

  public Instant getLastLoginAt() {
    return lastLoginAt;
  }

  public void setLastLoginAt(Instant lastLoginAt) {
    this.lastLoginAt = lastLoginAt;
  }

  public Integer getFailedCount() {
    return failedCount;
  }

  public void setFailedCount(Integer failedCount) {
    this.failedCount = failedCount;
  }

  public Instant getLockedUntil() {
    return lockedUntil;
  }

  public void setLockedUntil(Instant lockedUntil) {
    this.lockedUntil = lockedUntil;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
