package com.zhilian.zr.sys.controller;

import com.zhilian.zr.common.api.ApiResponse;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

  @PutMapping("/definitions")
  @PreAuthorize("hasAuthority('sys:metrics:update')")
  public ApiResponse<Void> update(@RequestBody Map<String, Object> req) {
    return ApiResponse.ok(null);
  }
}
