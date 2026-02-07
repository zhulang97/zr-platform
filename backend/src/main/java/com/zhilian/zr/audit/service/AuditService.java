package com.zhilian.zr.audit.service;

import jakarta.servlet.http.HttpServletRequest;

public interface AuditService {
  void log(HttpServletRequest request, String action, String resource, Object requestObj, String resultCode, long costMs);
}
