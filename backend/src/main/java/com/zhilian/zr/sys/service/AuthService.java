package com.zhilian.zr.sys.service;

import com.zhilian.zr.sys.dto.AuthDtos;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
  AuthDtos.TokenResponse login(AuthDtos.LoginRequest req, HttpServletRequest request, HttpServletResponse response);

  AuthDtos.TokenResponse refresh(HttpServletRequest request, HttpServletResponse response);

  void logout(HttpServletRequest request, HttpServletResponse response);

  AuthDtos.MeResponse me(long userId);
}
