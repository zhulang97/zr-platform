package com.zhilian.zr.assistant.controller;

import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.assistant.service.AssistantService;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

  private final AssistantService assistantService;

  public AssistantController(AssistantService assistantService) {
    this.assistantService = assistantService;
  }

  @PostMapping("/chat")
  @PreAuthorize("hasAuthority('assistant:chat')")
  public ApiResponse<Map<String, Object>> chat(@RequestBody Map<String, Object> req) {
    String text = req.containsKey("text") ? (String) req.get("text") : "";
    String currentPage = req.containsKey("currentPage") ? (String) req.get("currentPage") : "home";
    return ApiResponse.ok(assistantService.chat(text, currentPage));
  }

  @PostMapping("/parse")
  @PreAuthorize("hasAuthority('assistant:parse')")
  public ApiResponse<Map<String, Object>> parse(@RequestBody Map<String, Object> req) {
    String text = req.containsKey("text") ? (String) req.get("text") : "";
    return ApiResponse.ok(assistantService.parse(text));
  }

  @GetMapping("/suggestions")
  @PreAuthorize("hasAuthority('assistant:chat')")
  public ApiResponse<Map<String, Object>> suggestions() {
    return ApiResponse.ok(Map.of(
        "suggestions", java.util.List.of(
            "帮我查浦东新区二级肢体残疾的人",
            "显示有残车补贴的人员名单",
            "查询视力残疾一级的人员",
            "显示所有异常记录"
        )
    ));
  }
}