package com.zhilian.zr.importing.controller;

import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.importing.dto.ImportDtos.*;
import com.zhilian.zr.importing.service.ImportModuleService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
public class ImportModuleController {

    private final ImportModuleService importModuleService;

    public ImportModuleController(ImportModuleService importModuleService) {
        this.importModuleService = importModuleService;
    }

    @GetMapping("/modules")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ModuleDTO>> getModules(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(importModuleService.getAllModules(userId));
    }

    @GetMapping("/modules/category/{category}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ModuleDTO>> getModulesByCategory(
            @PathVariable String category,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(importModuleService.getModulesByCategory(category, userId));
    }

    @GetMapping("/modules/{moduleCode}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ModuleDTO> getModule(
            @PathVariable String moduleCode,
            @AuthenticationPrincipal Long userId) {
        List<ModuleDTO> modules = importModuleService.getAllModules(userId);
        return ApiResponse.ok(modules.stream()
                .filter(m -> m.code().equals(moduleCode))
                .findFirst()
                .orElse(null));
    }

    @GetMapping("/modules/{moduleCode}/fields")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ModuleFieldDTO>> getModuleFields(@PathVariable String moduleCode) {
        return ApiResponse.ok(importModuleService.getModuleFields(moduleCode));
    }

    @GetMapping("/modules/{moduleCode}/template")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable String moduleCode) {
        byte[] content = importModuleService.generateTemplate(moduleCode);
        String filename = URLEncoder.encode(moduleCode + "_template.xlsx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @PostMapping("/modules/{moduleCode}/upload")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UploadResultDTO> uploadFile(
            @PathVariable String moduleCode,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "strategy", defaultValue = "ID_CARD_MERGE") String strategy,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(importModuleService.uploadFile(moduleCode, file, userId));
    }

    @GetMapping("/batches/{batchId}")
    @PreAuthorize("hasAuthority('import:read')")
    public ApiResponse<BatchDTO> getBatch(@PathVariable long batchId) {
        return ApiResponse.ok(importModuleService.getBatchDetail(batchId));
    }

    @GetMapping("/batches")
    @PreAuthorize("hasAuthority('import:read')")
    public ApiResponse<Map<String, Object>> getBatches(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Long userId) {
        List<BatchDTO> batches = importModuleService.getBatches(userId, module, page, size);
        long totalElements = importModuleService.getBatchesCount(userId, module);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return ApiResponse.ok(Map.of(
                "content", batches,
                "totalElements", totalElements,
                "totalPages", totalPages,
                "size", size,
                "number", page
        ));
    }

    @GetMapping("/batches/{batchId}/mapping")
    @PreAuthorize("hasAuthority('import:read')")
    public ApiResponse<MappingPreviewDTO> getMapping(@PathVariable long batchId) {
        return ApiResponse.ok(importModuleService.previewMapping(batchId, Map.of()));
    }

    @PostMapping("/batches/{batchId}/auto-map")
    @PreAuthorize("hasAuthority('import:edit')")
    public ApiResponse<MappingPreviewDTO> autoMap(@PathVariable long batchId) {
        return ApiResponse.ok(importModuleService.autoMapFields(batchId));
    }

    @PostMapping("/batches/{batchId}/mapping")
    @PreAuthorize("hasAuthority('import:edit')")
    public ApiResponse<MappingPreviewDTO> saveMapping(
            @PathVariable long batchId,
            @RequestBody Map<String, String> fieldMapping) {
        return ApiResponse.ok(importModuleService.previewMapping(batchId, fieldMapping));
    }

    @GetMapping("/batches/{batchId}/preview")
    @PreAuthorize("hasAuthority('import:read')")
    public ApiResponse<Map<String, Object>> preview(@PathVariable long batchId) {
        return ApiResponse.ok(importModuleService.getPreviewData(batchId));
    }

    @PostMapping("/batches/{batchId}/execute")
    @PreAuthorize("hasAuthority('import:execute')")
    public ApiResponse<CommitResultDTO> execute(
            @PathVariable long batchId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Long userId) {
        String strategy = request.getOrDefault("strategy", "ID_CARD_MERGE");
        return ApiResponse.ok(importModuleService.commit(batchId, strategy, userId));
    }

    @GetMapping("/batches/{batchId}/progress")
    @PreAuthorize("hasAuthority('import:read')")
    public ApiResponse<Map<String, Object>> getProgress(@PathVariable long batchId) {
        BatchDTO batch = importModuleService.getBatchDetail(batchId);
        int total = batch.totalRows();
        int success = batch.successRows();
        int progress = total > 0 ? (success * 100 / total) : 0;
        return ApiResponse.ok(Map.of(
                "status", batch.status(),
                "progress", progress,
                "totalRows", total,
                "processedRows", success + batch.failedRows(),
                "successRows", success,
                "failedRows", batch.failedRows()
        ));
    }

    @GetMapping("/batches/{batchId}/logs")
    @PreAuthorize("hasAuthority('import:read')")
    public ApiResponse<List<LogDTO>> getLogs(@PathVariable long batchId) {
        return ApiResponse.ok(importModuleService.getBatchLogs(batchId));
    }

    @GetMapping("/batches/{batchId}/errors/export")
    @PreAuthorize("hasAuthority('import:export')")
    public ResponseEntity<byte[]> exportErrors(@PathVariable long batchId) {
        byte[] content = importModuleService.exportErrors(batchId);
        String filename = URLEncoder.encode("import_errors_" + batchId + ".xlsx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @GetMapping("/backups/{backupId}/download")
    @PreAuthorize("hasAuthority('import:backup:read')")
    public ResponseEntity<byte[]> downloadBackup(@PathVariable long backupId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"backup.xlsx\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new byte[0]);
    }

    @GetMapping("/batches/{batchId}/rows")
    @PreAuthorize("hasAuthority('import:read')")
    public ApiResponse<Map<String, Object>> getBatchRows(
            @PathVariable long batchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(importModuleService.getBatchRows(batchId, page, size, status));
    }
}
