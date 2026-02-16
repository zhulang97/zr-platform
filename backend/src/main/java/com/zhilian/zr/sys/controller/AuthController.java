package com.zhilian.zr.sys.controller;

import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.security.ZrPrincipal;
import com.zhilian.zr.sys.dto.AuthDtos;
import com.zhilian.zr.sys.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ApiResponse<AuthDtos.TokenResponse> login(
      @Valid @RequestBody AuthDtos.LoginRequest req,
      HttpServletRequest request,
      HttpServletResponse response) {
    return ApiResponse.ok(authService.login(req, request, response));
  }

  @PostMapping("/refresh")
  public ApiResponse<AuthDtos.TokenResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
    return ApiResponse.ok(authService.refresh(request, response));
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    authService.logout(request, response);
    return ApiResponse.ok(null);
  }

  @GetMapping("/me")
  public ApiResponse<AuthDtos.MeResponse> me(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ApiResponse.fail("未登录");
    }
    ZrPrincipal principal = (ZrPrincipal) authentication.getPrincipal();
    return ApiResponse.ok(authService.me(principal.userId()));
  }
}
