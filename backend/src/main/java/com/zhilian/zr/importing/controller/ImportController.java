package com.zhilian.zr.importing.controller;

import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.importing.service.ImportService;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
public class ImportController {

  private final ImportService importService;

  public ImportController(ImportService importService) {
    this.importService = importService;
  }

  @GetMapping("/templates/{type}")
  @PreAuthorize("hasAuthority('import:template:read')")
  public ApiResponse<Map<String, Object>> template(@PathVariable String type) {
    String csvTemplate = importService.generateTemplate(type);
    return ApiResponse.ok(Map.of(
        "type", type,
        "filename", type + "-template.csv",
        "content", csvTemplate
    ));
  }

  @PostMapping("/upload")
  @PreAuthorize("hasAuthority('import:upload')")
  public ApiResponse<Map<String, Object>> upload(
      @RequestParam(value = "type", required = false) String type,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "note", required = false) String note) {
    long batchId = importService.upload(type, file, note);
    return ApiResponse.ok(Map.of("batchId", batchId, "status", "UPLOADED"));
  }

  @GetMapping("/{batchId}/validate")
  @PreAuthorize("hasAuthority('import:validate:read')")
  public ApiResponse<Map<String, Object>> validate(@PathVariable long batchId) {
    return ApiResponse.ok(importService.validate(batchId));
  }

  @PostMapping("/{batchId}/commit")
  @PreAuthorize("hasAuthority('import:commit')")
  public ApiResponse<Map<String, Object>> commit(
      @PathVariable long batchId,
      @RequestBody Map<String, Object> req) {
    String strategy = req.containsKey("strategy") ? (String) req.get("strategy") : "MARK_ERROR";
    Map<String, Object> result = importService.commit(batchId, strategy);
    return ApiResponse.ok(result);
  }
}