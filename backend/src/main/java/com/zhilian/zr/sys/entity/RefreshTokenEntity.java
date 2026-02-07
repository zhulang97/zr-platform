package com.zhilian.zr.sys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("t_refresh_token")
public class RefreshTokenEntity {
  @TableId
  private Long id;
  private Long userId;
  private String tokenHash;
  private Instant expiresAt;
  private Instant revokedAt;
  private Instant createdAt;
  private String ip;
  private String ua;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public void setTokenHash(String tokenHash) {
    this.tokenHash = tokenHash;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(Instant revokedAt) {
    this.revokedAt = revokedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getUa() {
    return ua;
  }

  public void setUa(String ua) {
    this.ua = ua;
  }
}
