package com.zhilian.zr.stats.controller;

import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.stats.service.StatsService;
import com.zhilian.zr.audit.service.AuditService;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

  private final StatsService statsService;
  private final AuditService auditService;

  public StatsController(StatsService statsService, AuditService auditService) {
    this.statsService = statsService;
    this.auditService = auditService;
  }

  @PostMapping("/overview")
  @PreAuthorize("hasAuthority('stats:read')")
  public ApiResponse<Map<String, Object>> overview(@RequestBody Map<String, Object> req) {
    return ApiResponse.ok(statsService.overview(req));
  }

  @PostMapping("/disability-distribution")
  @PreAuthorize("hasAuthority('stats:read')")
  public ApiResponse<Map<String, Object>> disabilityDistribution(@RequestBody Map<String, Object> req) {
    return ApiResponse.ok(statsService.disabilityDistribution(req));
  }

  @PostMapping("/subsidy-coverage")
  @PreAuthorize("hasAuthority('stats:read')")
  public ApiResponse<Map<String, Object>> subsidyCoverage(@RequestBody Map<String, Object> req) {
    return ApiResponse.ok(statsService.subsidyCoverage(req));
  }

  @PostMapping("/by-district")
  @PreAuthorize("hasAuthority('stats:read')")
  public ApiResponse<Map<String, Object>> byDistrict(@RequestBody Map<String, Object> req) {
    return ApiResponse.ok(statsService.byDistrict(req));
  }

  @PostMapping("/export")
  @PreAuthorize("hasAuthority('stats:export')")
  public ResponseEntity<byte[]> export(@RequestBody Map<String, Object> req, jakarta.servlet.http.HttpServletRequest request) {
    long start = System.currentTimeMillis();
    byte[] bytes = statsService.exportCsv(req);
    auditService.log(request, "EXPORT", "stats", req, "OK", System.currentTimeMillis() - start);
    String filename = "stats-" + LocalDate.now() + ".csv";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(new MediaType("text", "csv"))
        .body(bytes);
  }
}