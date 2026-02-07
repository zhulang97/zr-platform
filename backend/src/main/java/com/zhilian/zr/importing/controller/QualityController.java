package com.zhilian.zr.importing.controller;

import com.zhilian.zr.common.api.ApiResponse;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quality")
public class QualityController {

  @PostMapping("/check")
  @PreAuthorize("hasAuthority('quality:check')")
  public ApiResponse<Map<String, Object>> check(@RequestBody Map<String, Object> req) {
    return ApiResponse.ok(Map.of("status", "todo"));
  }

  @GetMapping("/issues")
  @PreAuthorize("hasAuthority('quality:read')")
  public ApiResponse<List<Map<String, Object>>> issues() {
    return ApiResponse.ok(List.of());
  }
}
