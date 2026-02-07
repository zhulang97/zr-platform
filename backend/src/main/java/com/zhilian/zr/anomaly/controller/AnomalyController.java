package com.zhilian.zr.anomaly.controller;

import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.common.api.PageResponse;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.zhilian.zr.anomaly.dto.AnomalyDtos;
import com.zhilian.zr.anomaly.service.AnomalyService;
import com.zhilian.zr.audit.service.AuditService;
import java.time.LocalDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/anomalies")
public class AnomalyController {

  private final AnomalyService anomalyService;
  private final AuditService auditService;

  public AnomalyController(AnomalyService anomalyService, AuditService auditService) {
    this.anomalyService = anomalyService;
    this.auditService = auditService;
  }

  @PostMapping("/search")
  @PreAuthorize("hasAuthority('anomaly:search')")
  public ApiResponse<PageResponse<AnomalyDtos.AnomalyRow>> search(@RequestBody AnomalyDtos.SearchRequest req) {
    return ApiResponse.ok(anomalyService.search(req));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('anomaly:read')")
  public ApiResponse<AnomalyDtos.AnomalyDetail> detail(@PathVariable long id) {
    return ApiResponse.ok(anomalyService.detail(id));
  }

  @PostMapping("/{id}/status")
  @PreAuthorize("hasAuthority('anomaly:handle')")
  public ApiResponse<Void> updateStatus(@PathVariable long id, @RequestBody AnomalyDtos.StatusRequest req, HttpServletRequest request) {
    long start = System.currentTimeMillis();
    anomalyService.updateStatus(id, req);
    auditService.log(request, "UPDATE", "anomaly_status:" + id, req, "OK", System.currentTimeMillis() - start);
    return ApiResponse.ok(null);
  }

  @PostMapping("/export")
  @PreAuthorize("hasAuthority('anomaly:export')")
  public ResponseEntity<byte[]> export(@RequestBody AnomalyDtos.SearchRequest req, HttpServletRequest request) {
    long start = System.currentTimeMillis();
    byte[] bytes = anomalyService.exportCsv(req);
    auditService.log(request, "EXPORT", "anomalies", req, "OK", System.currentTimeMillis() - start);
    String filename = "anomalies-" + LocalDate.now() + ".csv";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(new MediaType("text", "csv"))
        .body(bytes);
  }
}
