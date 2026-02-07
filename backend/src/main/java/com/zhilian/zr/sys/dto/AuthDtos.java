package com.zhilian.zr.sys.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public final class AuthDtos {
  private AuthDtos() {
  }

  public record LoginRequest(
      @NotBlank String username,
      @NotBlank String password,
      String captcha
  ) {
  }

  public record TokenResponse(
      String accessToken,
      long expiresInSeconds
  ) {
  }

  public record MeResponse(
      long userId,
      String username,
      String displayName,
      List<String> permissions,
      List<MenuNode> menus,
      List<Long> districtScope
  ) {
  }

  public record MenuNode(
      String permCode,
      String name,
      String path,
      int sort,
      List<MenuNode> children
  ) {
  }
}
