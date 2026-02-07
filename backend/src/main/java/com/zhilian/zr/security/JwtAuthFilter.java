package com.zhilian.zr.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtTokenService jwtTokenService;

  public JwtAuthFilter(JwtTokenService jwtTokenService) {
    this.jwtTokenService = jwtTokenService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring("Bearer ".length()).trim();
      try {
        Claims claims = jwtTokenService.parse(token);
        String sub = claims.getSubject();
        String username = Objects.toString(claims.get("u"), null);
        Object a = claims.get("a");
        List<String> authorities = (a instanceof List<?> list)
            ? list.stream().map(String::valueOf).collect(Collectors.toList())
            : List.of();
        Collection<GrantedAuthority> granted = authorities.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        ZrPrincipal principal = new ZrPrincipal(Long.parseLong(sub), username);
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, granted);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (Exception ignore) {
        SecurityContextHolder.clearContext();
      }
    }

    filterChain.doFilter(request, response);
  }
}
