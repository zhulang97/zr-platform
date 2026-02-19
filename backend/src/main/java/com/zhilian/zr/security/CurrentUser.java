package com.zhilian.zr.security;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
  private CurrentUser() {
  }

  public static long userId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof ZrPrincipal p)) {
      throw new SecurityException("请先登录");
    }
    return p.userId();
  }

  public static Set<String> authorities() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      return Set.of();
    }
    return auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toSet());
  }

  public static boolean hasAuthority(String code) {
    return authorities().contains(code);
  }
}
