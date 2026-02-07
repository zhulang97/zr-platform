package com.zhilian.zr.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zr.common.ids.IdGenerator;
import com.zhilian.zr.security.JwtTokenService;
import com.zhilian.zr.security.ZrSecurityProperties;
import com.zhilian.zr.security.DataScopeService;
import com.zhilian.zr.sys.dto.AuthDtos;
import com.zhilian.zr.sys.entity.RefreshTokenEntity;
import com.zhilian.zr.sys.entity.UserEntity;
import com.zhilian.zr.sys.mapper.PermissionMapper;
import com.zhilian.zr.sys.mapper.RefreshTokenMapper;
import com.zhilian.zr.sys.mapper.UserDataScopeMapper;
import com.zhilian.zr.sys.mapper.UserMapper;
import com.zhilian.zr.sys.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
  private static final String REFRESH_COOKIE = "ZR_REFRESH";

  private final UserMapper userMapper;
  private final PermissionMapper permissionMapper;
  private final UserDataScopeMapper userDataScopeMapper;
  private final RefreshTokenMapper refreshTokenMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenService jwtTokenService;
  private final ZrSecurityProperties securityProperties;
  private final DataScopeService dataScopeService;

  public AuthServiceImpl(
      UserMapper userMapper,
      PermissionMapper permissionMapper,
      UserDataScopeMapper userDataScopeMapper,
      RefreshTokenMapper refreshTokenMapper,
      PasswordEncoder passwordEncoder,
      JwtTokenService jwtTokenService,
      ZrSecurityProperties securityProperties,
      DataScopeService dataScopeService) {
    this.userMapper = userMapper;
    this.permissionMapper = permissionMapper;
    this.userDataScopeMapper = userDataScopeMapper;
    this.refreshTokenMapper = refreshTokenMapper;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenService = jwtTokenService;
    this.securityProperties = securityProperties;
    this.dataScopeService = dataScopeService;
  }

  @Override
  public AuthDtos.TokenResponse login(AuthDtos.LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
    UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
        .eq(UserEntity::getUsername, req.username()));
    if (user == null || user.getStatus() == null || user.getStatus() != 1) {
      throw new IllegalArgumentException("Invalid username or password");
    }
    if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
      throw new IllegalStateException("Account is locked");
    }
    if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
      bumpFailed(user);
      throw new IllegalArgumentException("Invalid username or password");
    }
    resetFailed(user);

    List<String> perms = permissionMapper.listCodesByUserId(user.getUserId());
    String access = jwtTokenService.mintAccessToken(user.getUserId(), user.getUsername(), perms);
    mintRefreshCookie(user.getUserId(), request, response);
    return new AuthDtos.TokenResponse(access, securityProperties.accessTtlSeconds());
  }

  @Override
  public AuthDtos.TokenResponse refresh(HttpServletRequest request, HttpServletResponse response) {
    String refresh = readCookie(request, REFRESH_COOKIE);
    if (refresh == null) {
      throw new IllegalArgumentException("Missing refresh token");
    }
    String hash = sha256Hex(refresh);
    RefreshTokenEntity token = refreshTokenMapper.selectOne(new LambdaQueryWrapper<RefreshTokenEntity>()
        .eq(RefreshTokenEntity::getTokenHash, hash)
        .isNull(RefreshTokenEntity::getRevokedAt));
    if (token == null || token.getExpiresAt().isBefore(Instant.now())) {
      throw new IllegalArgumentException("Invalid refresh token");
    }
    UserEntity user = userMapper.selectById(token.getUserId());
    if (user == null || user.getStatus() == null || user.getStatus() != 1) {
      throw new IllegalArgumentException("Invalid user");
    }
    List<String> perms = permissionMapper.listCodesByUserId(user.getUserId());
    String access = jwtTokenService.mintAccessToken(user.getUserId(), user.getUsername(), perms);
    return new AuthDtos.TokenResponse(access, securityProperties.accessTtlSeconds());
  }

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    String refresh = readCookie(request, REFRESH_COOKIE);
    if (refresh != null) {
      String hash = sha256Hex(refresh);
      RefreshTokenEntity token = refreshTokenMapper.selectOne(new LambdaQueryWrapper<RefreshTokenEntity>()
          .eq(RefreshTokenEntity::getTokenHash, hash)
          .isNull(RefreshTokenEntity::getRevokedAt));
      if (token != null) {
        token.setRevokedAt(Instant.now());
        refreshTokenMapper.updateById(token);
      }
    }
    clearCookie(response, REFRESH_COOKIE);
  }

  @Override
  public AuthDtos.MeResponse me(long userId) {
    UserEntity user = userMapper.selectById(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found");
    }
    List<String> perms = permissionMapper.listCodesByUserId(userId);
    var menus = buildMenuTree(permissionMapper.listByUserId(userId));
    List<Long> scope = dataScopeService.districtScopeOrNullForAdmin(userId, new java.util.HashSet<>(perms));
    return new AuthDtos.MeResponse(
        userId,
        user.getUsername(),
        user.getDisplayName(),
        perms,
        menus,
        scope);
  }

  private void bumpFailed(UserEntity user) {
    int c = user.getFailedCount() == null ? 0 : user.getFailedCount();
    c++;
    user.setFailedCount(c);
    if (c >= 5) {
      user.setLockedUntil(Instant.now().plusSeconds(30 * 60));
    }
    user.setUpdatedAt(Instant.now());
    userMapper.updateById(user);
  }

  private void resetFailed(UserEntity user) {
    user.setFailedCount(0);
    user.setLockedUntil(null);
    user.setLastLoginAt(Instant.now());
    user.setUpdatedAt(Instant.now());
    userMapper.updateById(user);
  }

  private void mintRefreshCookie(long userId, HttpServletRequest request, HttpServletResponse response) {
    String token = UUID.randomUUID().toString().replace("-", "");
    RefreshTokenEntity e = new RefreshTokenEntity();
    e.setId(IdGenerator.nextId());
    e.setUserId(userId);
    e.setTokenHash(sha256Hex(token));
    e.setCreatedAt(Instant.now());
    e.setExpiresAt(Instant.now().plusSeconds(securityProperties.refreshTtlSeconds()));
    e.setIp(request.getRemoteAddr());
    e.setUa(Optional.ofNullable(request.getHeader("User-Agent")).orElse(""));
    refreshTokenMapper.insert(e);

    Cookie cookie = new Cookie(REFRESH_COOKIE, token);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge((int) securityProperties.refreshTtlSeconds());
    response.addCookie(cookie);
  }

  private static void clearCookie(HttpServletResponse response, String name) {
    Cookie cookie = new Cookie(name, "");
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  private static String readCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie c : cookies) {
      if (name.equals(c.getName())) {
        return c.getValue();
      }
    }
    return null;
  }

  private static String sha256Hex(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(bytes.length * 2);
      for (byte b : bytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static List<AuthDtos.MenuNode> buildMenuTree(List<com.zhilian.zr.sys.entity.PermissionEntity> perms) {
    List<com.zhilian.zr.sys.entity.PermissionEntity> menus = perms.stream()
        .filter(p -> "MENU".equalsIgnoreCase(p.getPermType()))
        .sorted(Comparator.comparingInt(p -> p.getSort() == null ? 0 : p.getSort()))
        .toList();

    Map<Long, List<com.zhilian.zr.sys.entity.PermissionEntity>> byParent = menus.stream()
        .collect(Collectors.groupingBy(p -> p.getParentId() == null ? 0L : p.getParentId()));

    return buildNodes(byParent, 0L);
  }

  private static List<AuthDtos.MenuNode> buildNodes(Map<Long, List<com.zhilian.zr.sys.entity.PermissionEntity>> byParent, long parentId) {
    List<com.zhilian.zr.sys.entity.PermissionEntity> children = byParent.getOrDefault(parentId, List.of());
    return children.stream()
        .sorted(Comparator.comparingInt(p -> p.getSort() == null ? 0 : p.getSort()))
        .map(p -> new AuthDtos.MenuNode(
            p.getPermCode(),
            p.getName(),
            p.getPath(),
            p.getSort() == null ? 0 : p.getSort(),
            buildNodes(byParent, p.getPermId())))
        .toList();
  }
}
