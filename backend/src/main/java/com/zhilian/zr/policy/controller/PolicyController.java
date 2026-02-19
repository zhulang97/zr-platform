package com.zhilian.zr.policy.controller;

import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.policy.dto.*;
import com.zhilian.zr.policy.entity.PolicyDocument;
import com.zhilian.zr.policy.service.PolicyService;
import com.zhilian.zr.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {
    
    private final PolicyService policyService;
    
    /**
     * 直接上传文件（通过后端代理到OSS）
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('policy:create')")
    public ApiResponse<PolicyUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {
        Long userId = CurrentUser.userId();
        PolicyUploadResponse response = policyService.uploadFile(userId, file, title);
        return ApiResponse.ok(response);
    }
    
    /**
     * 获取OSS上传URL
     */
    @PostMapping("/upload-url")
    @PreAuthorize("hasAuthority('policy:create')")
    public ApiResponse<PolicyUploadResponse> getUploadUrl(@RequestBody Map<String, String> request) {
        Long userId = CurrentUser.userId();
        String fileName = request.get("fileName");
        String fileType = request.get("fileType");
        
        PolicyUploadResponse response = policyService.getUploadUrl(userId, fileName, fileType);
        return ApiResponse.ok(response);
    }
    
    /**
     * 确认上传完成
     */
    @PostMapping("/{policyId}/confirm")
    @PreAuthorize("hasAuthority('policy:create')")
    public ApiResponse<Void> confirmUpload(@PathVariable Long policyId) {
        Long userId = CurrentUser.userId();
        policyService.confirmUpload(policyId, userId);
        return ApiResponse.ok(null);
    }
    
    /**
     * 分析政策
     */
    @PostMapping("/{policyId}/analyze")
    @PreAuthorize("hasAuthority('policy:analyze')")
    public ApiResponse<PolicyAnalysisResult> analyzePolicy(
            @PathVariable Long policyId,
            @RequestBody Map<String, String> request) {
        Long userId = CurrentUser.userId();
        String content = request.get("content");
        
        PolicyAnalysisResult result = policyService.analyzePolicy(policyId, content, userId);
        return ApiResponse.ok(result);
    }
    
    /**
     * 根据政策查询人员
     */
    @PostMapping("/query")
    @PreAuthorize("hasAuthority('policy:read')")
    public ApiResponse<PolicyQueryResult> queryPersons(@RequestBody @Valid PolicyQueryRequest request) {
        Long userId = CurrentUser.userId();
        PolicyQueryResult result = policyService.queryPersons(request, userId);
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取政策列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('policy:read')")
    public ApiResponse<List<PolicyDocumentVO>> getPolicyList() {
        Long userId = CurrentUser.userId();
        List<PolicyDocumentVO> list = policyService.getPolicyList(userId);
        return ApiResponse.ok(list);
    }
    
    /**
     * 获取政策详情
     */
    @GetMapping("/{policyId}")
    @PreAuthorize("hasAuthority('policy:read')")
    public ApiResponse<PolicyDocument> getPolicyDetail(@PathVariable Long policyId) {
        Long userId = CurrentUser.userId();
        PolicyDocument document = policyService.getPolicyDetail(policyId, userId);
        return ApiResponse.ok(document);
    }
    
    /**
     * 获取政策的所有版本
     */
    @GetMapping("/{policyId}/versions")
    @PreAuthorize("hasAuthority('policy:read')")
    public ApiResponse<List<PolicyAnalysisResult>> getPolicyVersions(@PathVariable Long policyId) {
        Long userId = CurrentUser.userId();
        List<PolicyAnalysisResult> versions = policyService.getPolicyVersions(policyId, userId);
        return ApiResponse.ok(versions);
    }
    
    /**
     * 更新政策标题
     */
    @PutMapping("/{policyId}/title")
    @PreAuthorize("hasAuthority('policy:update')")
    public ApiResponse<Void> updateTitle(
            @PathVariable Long policyId,
            @RequestBody Map<String, String> request) {
        Long userId = CurrentUser.userId();
        String title = request.get("title");
        policyService.updatePolicyTitle(policyId, title, userId);
        return ApiResponse.ok(null);
    }
    
    /**
     * 删除政策
     */
    @DeleteMapping("/{policyId}")
    @PreAuthorize("hasAuthority('policy:delete')")
    public ApiResponse<Void> deletePolicy(@PathVariable Long policyId) {
        Long userId = CurrentUser.userId();
        policyService.deletePolicy(policyId, userId);
        return ApiResponse.ok(null);
    }
    
    /**
     * 获取PDF预览信息
     */
    @GetMapping("/{policyId}/preview")
    @PreAuthorize("hasAuthority('policy:read')")
    public ApiResponse<Map<String, Object>> getPdfPreviewInfo(@PathVariable Long policyId) {
        Long userId = CurrentUser.userId();
        Map<String, Object> info = policyService.getPdfPreviewInfo(policyId, userId);
        return ApiResponse.ok(info);
    }
    
    /**
     * 转换PDF为图片
     */
    @PostMapping("/{policyId}/convert")
    @PreAuthorize("hasAuthority('policy:read')")
    public ApiResponse<List<String>> convertPdfToImages(@PathVariable Long policyId) {
        Long userId = CurrentUser.userId();
        List<String> imageUrls = policyService.convertPdfToImages(policyId, userId);
        return ApiResponse.ok(imageUrls);
    }
}
