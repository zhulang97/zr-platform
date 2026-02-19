package com.zhilian.zr.common.web;

import com.zhilian.zr.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> badRequest(IllegalArgumentException e) {
    return ApiResponse.fail(e.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiResponse<Void> conflict(IllegalStateException e) {
    return ApiResponse.fail(e.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ApiResponse<Void> accessDenied(AccessDeniedException e) {
    return ApiResponse.fail("权限不足");
  }

  @ExceptionHandler(SecurityException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiResponse<Void> securityException(SecurityException e) {
    return ApiResponse.fail(e.getMessage());
  }
}
