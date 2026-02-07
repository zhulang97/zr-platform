package com.zhilian.zr.audit.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhilian.zr.audit.mapper.AuditLogMapper;
import com.zhilian.zr.audit.service.AuditService;
import com.zhilian.zr.common.ids.IdGenerator;
import com.zhilian.zr.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AuditServiceImpl implements AuditService {
  private final AuditLogMapper auditLogMapper;
  private final ObjectMapper objectMapper;

  public AuditServiceImpl(AuditLogMapper auditLogMapper, ObjectMapper objectMapper) {
    this.auditLogMapper = auditLogMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public void log(HttpServletRequest request, String action, String resource, Object requestObj, String resultCode, long costMs) {
    String reqJson = null;
    try {
      if (requestObj != null) {
        reqJson = objectMapper.writeValueAsString(requestObj);
      }
    } catch (Exception ignore) {
      reqJson = null;
    }

    Long userId = null;
    try {
      userId = CurrentUser.userId();
    } catch (Exception ignore) {
      userId = null;
    }

    String ip = request == null ? null : request.getRemoteAddr();
    String ua = request == null ? null : request.getHeader("User-Agent");

    auditLogMapper.insert(
        IdGenerator.nextId(),
        userId,
        action,
        resource,
        reqJson,
        resultCode,
        costMs,
        Instant.now(),
        ip,
        ua
    );
  }
}
