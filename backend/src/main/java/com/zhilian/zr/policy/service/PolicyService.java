package com.zhilian.zr.policy.service;

import com.alibaba.fastjson2.JSON;
import com.zhilian.zr.assistant.dto.AssistantDsl;
import com.zhilian.zr.common.api.PageResponse;
import com.zhilian.zr.person.dto.PersonDtos;
import com.zhilian.zr.person.entity.PersonIndexEntity;
import com.zhilian.zr.person.service.PersonIndexService;
import com.zhilian.zr.person.service.PersonService;
import com.zhilian.zr.policy.dto.*;
import com.zhilian.zr.policy.entity.PolicyAnalysis;
import com.zhilian.zr.policy.entity.PolicyDocument;
import com.zhilian.zr.policy.entity.PolicyQueryLog;
import com.zhilian.zr.policy.mapper.PolicyAnalysisMapper;
import com.zhilian.zr.policy.mapper.PolicyMapper;
import com.zhilian.zr.policy.util.PdfImageService;
import com.zhilian.zr.security.CurrentUser;
import com.zhilian.zr.security.DataScopeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyService {
    
    private final PolicyMapper policyMapper;
    private final PolicyAnalysisMapper analysisMapper;
    private final PolicyDocumentService documentService;
    private final PolicyAnalysisService analysisService;
    private final FileExtractService fileExtractService;
    private final PdfImageService pdfImageService;
    private final PersonService personService;
    private final PersonIndexService personIndexService;
    private final DataScopeService dataScopeService;
    
    /**
     * 直接上传文件（通过后端代理到OSS）
     */
    @Transactional
    public PolicyUploadResponse uploadFile(Long userId, MultipartFile file, String title) {
        try {
            String fileName = file.getOriginalFilename();
            String fileType = file.getContentType();
            long fileSize = file.getSize();
            
            // 生成OSS Key
            PolicyUploadResponse response = documentService.generateUploadUrl(userId, fileName, fileType);
            String ossKey = response.getOssKey();
            
            // 读取文件内容到字节数组（避免InputStream重置问题）
            byte[] fileBytes = file.getBytes();
            
            // 上传到OSS
            documentService.uploadFile(ossKey, fileBytes, fileType);
            
            // 创建政策记录
            PolicyDocument document = new PolicyDocument();
            document.setUserId(userId);
            document.setTitle(title != null ? title : fileName);
            document.setOssKey(ossKey);
            document.setOssUrl(documentService.getFileUrl(ossKey));
            document.setFileName(fileName);
            document.setFileType(fileType);
            document.setFileSize(fileSize);
            document.setStatus("ACTIVE");
            document.setCreatedAt(LocalDateTime.now());
            document.setUpdatedAt(LocalDateTime.now());
            
            policyMapper.insert(document);
            
            response.setPolicyId(document.getPolicyId());
            response.setTitle(document.getTitle());
            response.setCreatedAt(document.getCreatedAt());
            
            return response;
        } catch (Exception e) {
            log.error("Failed to upload policy file", e);
            throw new RuntimeException("上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取政策上传URL
     */
    public PolicyUploadResponse getUploadUrl(Long userId, String fileName, String fileType) {
        PolicyUploadResponse response = documentService.generateUploadUrl(userId, fileName, fileType);
        
        // 创建政策记录（状态为PENDING，等待上传完成）
        PolicyDocument document = new PolicyDocument();
        document.setUserId(userId);
        document.setTitle(fileName);
        document.setOssKey(response.getOssKey());
        document.setOssUrl("");  // 占位，上传确认后会更新
        document.setFileName(fileName);
        document.setFileType(fileType);
        document.setStatus("PENDING");
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        
        policyMapper.insert(document);
        response.setPolicyId(document.getPolicyId());
        
        return response;
    }
    
    /**
     * 确认政策上传完成
     */
    @Transactional
    public void confirmUpload(Long policyId, Long userId) {
        PolicyDocument document = policyMapper.selectById(policyId);
        if (document == null || !document.getUserId().equals(userId)) {
            throw new RuntimeException("政策文档不存在或无权限");
        }
        
        // 获取文件URL
        String ossUrl = documentService.getFileUrl(document.getOssKey());
        document.setOssUrl(ossUrl);
        document.setStatus("ACTIVE");
        
        // 获取文件信息
        var fileInfo = fileExtractService.getFileInfo(document.getOssKey());
        document.setFileSize(fileInfo.getSize());
        
        policyMapper.updateById(document);
    }
    
    /**
     * 分析政策
     */
    @Transactional
    public PolicyAnalysisResult analyzePolicy(Long policyId, String content, Long userId) {
        PolicyDocument document = policyMapper.selectById(policyId);
        if (document == null || !document.getUserId().equals(userId)) {
            throw new RuntimeException("政策文档不存在或无权限");
        }
        
        // 如果没有提供内容，从文件提取
        if (content == null || content.isEmpty()) {
            content = fileExtractService.extractText(document.getOssKey());
        }
        
        document.setContentLength(content.length());
        policyMapper.updateById(document);
        
        // 执行分析
        PolicyConditions conditions = analysisService.analyzePolicy(content);
        
        // 获取下一个版本号
        Integer nextVersion = policyMapper.selectNextVersion(policyId);
        
        // 清除之前的最新版本标记
        policyMapper.clearLatestVersion(policyId);
        
        // 保存分析结果
        PolicyAnalysis analysis = new PolicyAnalysis();
        analysis.setPolicyId(policyId);
        analysis.setVersion(nextVersion);
        analysis.setIsLatest("Y");
        analysis.setConditionsJson(JSON.toJSONString(conditions));
        analysis.setExplanation(conditions.getExplanation());
        
        // 计算分段信息
        int segmentSize = 4000;
        int totalSegments = (int) Math.ceil((double) content.length() / segmentSize);
        analysis.setTotalSegments(totalSegments);
        analysis.setAnalyzedSegments(totalSegments);
        analysis.setCreatedAt(LocalDateTime.now());
        
        analysisMapper.insert(analysis);
        
        // 构建返回结果
        PolicyAnalysisResult result = new PolicyAnalysisResult();
        result.setAnalysisId(analysis.getAnalysisId());
        result.setVersion(analysis.getVersion());
        result.setIsLatest(true);
        result.setConditions(conditions);
        result.setExplanation(analysis.getExplanation());
        result.setAnalyzedSegments(analysis.getAnalyzedSegments());
        result.setTotalSegments(analysis.getTotalSegments());
        result.setCreatedAt(analysis.getCreatedAt());
        
        // 如果有旧版本，计算差异
        if (nextVersion > 1) {
            PolicyAnalysis previousAnalysis = analysisMapper.selectByPolicyId(policyId).stream()
                .filter(a -> a.getVersion() == nextVersion - 1)
                .findFirst()
                .orElse(null);
            
            if (previousAnalysis != null) {
                PolicyConditions oldConditions = JSON.parseObject(
                    previousAnalysis.getConditionsJson(), 
                    PolicyConditions.class
                );
                List<ConditionDiff> diffs = analysisService.calculateDiff(oldConditions, conditions);
                result.setDiffs(diffs);
            }
        }
        
        return result;
    }
    
    /**
     * 根据政策查询人员
     */
    public PolicyQueryResult queryPersons(PolicyQueryRequest request, Long userId) {
        PolicyAnalysis analysis = analysisMapper.selectById(request.getAnalysisId());
        if (analysis == null) {
            throw new RuntimeException("分析记录不存在");
        }
        
        PolicyDocument document = policyMapper.selectById(analysis.getPolicyId());
        if (document == null || !document.getUserId().equals(userId)) {
            throw new RuntimeException("无权限访问此政策");
        }
        
        // 使用用户调整后的条件或原始条件
        PolicyConditions conditions = request.getConditions() != null 
            ? request.getConditions() 
            : JSON.parseObject(analysis.getConditionsJson(), PolicyConditions.class);
        
        // 从PersonIndex查询
        int pageNo = request.getPageNo() != null ? request.getPageNo() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 20;
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.zhilian.zr.person.entity.PersonIndexEntity> page = 
            personIndexService.search(conditions, pageNo, pageSize);
        
        List<PersonIndexEntity> persons = page.getRecords();
        List<PersonDtos.PersonRow> items = persons.stream().map(p -> {
            return new PersonDtos.PersonRow(
                p.getIdCard() != null ? p.getIdCard().hashCode() : 0L,
                p.getName() != null ? p.getName().substring(0, 1) + "**" : null,
                p.getIdCard() != null ? p.getIdCard().substring(0, 6) + "********" + p.getIdCard().substring(14) : null,
                p.getDisabilityCategory(),
                null,
                p.getDisabilityLevel(),
                null,
                p.getDistrict(),
                p.getStreet(),
                null,
                null,
                false,
                false,
                false,
                false
            );
        }).collect(Collectors.toList());
        
        PolicyQueryResult result = new PolicyQueryResult();
        result.setPolicyId(document.getPolicyId());
        result.setPolicyTitle(document.getTitle());
        result.setVersion(analysis.getVersion());
        result.setConditions(conditions);
        result.setTotal(page.getTotal());
        result.setItems(items);
        
        return result;
    }

    public PolicyQueryResult queryPersonsFromIndex(PolicyQueryRequest request, Long userId) {
        PolicyAnalysis analysis = analysisMapper.selectById(request.getAnalysisId());
        if (analysis == null) {
            throw new RuntimeException("分析记录不存在");
        }
        
        PolicyDocument document = policyMapper.selectById(analysis.getPolicyId());
        if (document == null || !document.getUserId().equals(userId)) {
            throw new RuntimeException("无权限访问此政策");
        }
        
        PolicyConditions conditions = request.getConditions() != null 
            ? request.getConditions() 
            : JSON.parseObject(analysis.getConditionsJson(), PolicyConditions.class);
        
        int pageNo = request.getPageNo() != null ? request.getPageNo() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 20;
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.zhilian.zr.person.entity.PersonIndexEntity> page = 
            personIndexService.search(conditions, pageNo, pageSize);
        
        List<PersonIndexEntity> persons = page.getRecords();
        List<PersonDtos.PersonRow> items = persons.stream().map(p -> {
            return new PersonDtos.PersonRow(
                p.getIdCard() != null ? p.getIdCard().hashCode() : 0L,
                p.getName() != null ? p.getName().substring(0, 1) + "**" : null,
                p.getIdCard() != null ? p.getIdCard().substring(0, 6) + "********" + p.getIdCard().substring(14) : null,
                p.getDisabilityCategory(),
                null,
                p.getDisabilityLevel(),
                null,
                p.getDistrict(),
                p.getStreet(),
                null,
                null,
                false,
                false,
                false,
                false
            );
        }).collect(Collectors.toList());
        
        PolicyQueryResult result = new PolicyQueryResult();
        result.setPolicyId(document.getPolicyId());
        result.setPolicyTitle(document.getTitle());
        result.setVersion(analysis.getVersion());
        result.setConditions(conditions);
        result.setTotal(page.getTotal());
        result.setItems(items);
        
        return result;
    }
    
    /**
     * 获取用户的政策列表
     */
    public List<PolicyDocumentVO> getPolicyList(Long userId) {
        List<PolicyDocument> documents = policyMapper.selectListWithLatestVersion(userId);
        
        return documents.stream().map(doc -> {
            PolicyDocumentVO vo = new PolicyDocumentVO();
            vo.setPolicyId(doc.getPolicyId());
            vo.setTitle(doc.getTitle());
            vo.setFileName(doc.getFileName());
            vo.setFileType(doc.getFileType());
            vo.setFileSize(doc.getFileSize());
            vo.setContentLength(doc.getContentLength());
            vo.setStatus(doc.getStatus());
            vo.setCreatedAt(doc.getCreatedAt());
            vo.setUpdatedAt(doc.getUpdatedAt());
            
            // 设置最新版本信息
            try {
                PolicyAnalysis latestAnalysis = analysisMapper.selectLatestByPolicyId(doc.getPolicyId());
                if (latestAnalysis != null) {
                    vo.setLatestVersion(latestAnalysis.getVersion());
                    vo.setLatestExplanation(latestAnalysis.getExplanation());
                }
            } catch (Exception e) {
                log.warn("Failed to get latest analysis for policy: {}", doc.getPolicyId(), e);
            }
            
            return vo;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取政策详情
     */
    public PolicyDocument getPolicyDetail(Long policyId, Long userId) {
        PolicyDocument document = policyMapper.selectById(policyId);
        if (document == null || !document.getUserId().equals(userId)) {
            throw new RuntimeException("政策文档不存在或无权限");
        }
        
        // 刷新URL（防止过期）
        String ossUrl = documentService.getFileUrl(document.getOssKey());
        document.setOssUrl(ossUrl);
        
        return document;
    }
    
    /**
     * 获取政策的所有分析版本
     */
    public List<PolicyAnalysisResult> getPolicyVersions(Long policyId, Long userId) {
        PolicyDocument document = policyMapper.selectById(policyId);
        if (document == null || !document.getUserId().equals(userId)) {
            throw new RuntimeException("政策文档不存在或无权限");
        }
        
        List<PolicyAnalysis> analyses = analysisMapper.selectByPolicyId(policyId);
        
        return analyses.stream().map(analysis -> {
            PolicyAnalysisResult result = new PolicyAnalysisResult();
            result.setAnalysisId(analysis.getAnalysisId());
            result.setVersion(analysis.getVersion());
            result.setIsLatest("Y".equals(analysis.getIsLatest()));
            result.setConditions(JSON.parseObject(analysis.getConditionsJson(), PolicyConditions.class));
            result.setExplanation(analysis.getExplanation());
            result.setAnalyzedSegments(analysis.getAnalyzedSegments());
            result.setTotalSegments(analysis.getTotalSegments());
            result.setCreatedAt(analysis.getCreatedAt());
            return result;
        }).collect(Collectors.toList());
    }
    
    /**
     * 更新政策标题
     */
    @Transactional
    public void updatePolicyTitle(Long policyId, String title, Long userId) {
        PolicyDocument document = policyMapper.selectById(policyId);
        if (document == null || !document.getUserId().equals(userId)) {
            throw new RuntimeException("政策文档不存在或无权限");
        }
        
        document.setTitle(title);
        policyMapper.updateById(document);
    }
    
    /**
     * 删除政策
     */
    @Transactional
    public void deletePolicy(Long policyId, Long userId) {
        PolicyDocument document = policyMapper.selectById(policyId);
        if (document == null || !document.getUserId().equals(userId)) {
            throw new RuntimeException("政策文档不存在或无权限");
        }
        
        // 删除OSS文件
        try {
            documentService.deleteFile(document.getOssKey());
        } catch (Exception e) {
            log.warn("Failed to delete OSS file: {}", document.getOssKey(), e);
        }
        
        // 逻辑删除
        document.setStatus("DELETED");
        policyMapper.updateById(document);
    }
    
    /**
     * 获取PDF预览信息
     */
    public Map<String, Object> getPdfPreviewInfo(Long policyId, Long userId) {
        PolicyDocument document = policyMapper.selectById(policyId);
        if (document == null || !document.getUserId().equals(userId)) {
            throw new RuntimeException("政策文档不存在或无权限");
        }
        
        Map<String, Object> result = new HashMap<>();
        
        // 检查是否是PDF文件
        if (!"application/pdf".equals(document.getFileType()) && 
            !document.getFileName().toLowerCase().endsWith(".pdf")) {
            result.put("isPdf", false);
            result.put("message", "非PDF文件，不支持图片预览");
            return result;
        }
        
        result.put("isPdf", true);
        result.put("policyId", policyId);
        
        // 检查是否已经转换过
        boolean isConverted = pdfImageService.isPdfConverted(document.getOssKey(), policyId, userId);
        result.put("isConverted", isConverted);
        
        if (isConverted) {
            // 获取已转换的图片URL
            List<String> imageUrls = pdfImageService.getConvertedImageUrls(document.getOssKey(), policyId, userId);
            result.put("imageUrls", imageUrls);
            result.put("pageCount", imageUrls.size());
        } else {
            // 获取PDF信息
            var pdfInfo = pdfImageService.getPdfPreviewInfo(document.getOssKey());
            result.put("pageCount", pdfInfo.getPageCount());
            result.put("message", "PDF尚未转换为图片，请点击转换按钮");
        }
        
        return result;
    }
    
    /**
     * 转换PDF为图片
     */
    public List<String> convertPdfToImages(Long policyId, Long userId) {
        PolicyDocument document = policyMapper.selectById(policyId);
        if (document == null || !document.getUserId().equals(userId)) {
            throw new RuntimeException("政策文档不存在或无权限");
        }
        
        // 检查是否是PDF文件
        if (!"application/pdf".equals(document.getFileType()) && 
            !document.getFileName().toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("非PDF文件，无法转换");
        }
        
        // 检查是否已经转换过
        if (pdfImageService.isPdfConverted(document.getOssKey(), policyId, userId)) {
            log.info("PDF already converted, returning existing images");
            return pdfImageService.getConvertedImageUrls(document.getOssKey(), policyId, userId);
        }
        
        // 执行转换
        log.info("Converting PDF to images for policy: {}", policyId);
        List<String> imageUrls = pdfImageService.convertPdfToImages(document.getOssKey(), policyId, userId);
        log.info("Successfully converted PDF to {} images", imageUrls.size());
        
        return imageUrls;
    }
}
