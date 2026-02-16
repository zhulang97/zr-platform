package com.zhilian.zr.importing.service;

import com.zhilian.zr.importing.dto.ImportDtos.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ImportModuleService {
    
    List<ModuleDTO> getAllModules(Long userId);
    
    List<ModuleDTO> getModulesByCategory(String category, Long userId);
    
    List<ModuleFieldDTO> getModuleFields(String moduleCode);
    
    byte[] generateTemplate(String moduleCode);
    
    UploadResultDTO uploadFile(String moduleCode, MultipartFile file, Long userId);
    
    MappingPreviewDTO previewMapping(long batchId, Map<String, String> fieldMapping);
    
    MappingPreviewDTO autoMapFields(long batchId);
    
    ValidateResultDTO validate(long batchId);
    
    Map<String, Object> getPreviewData(long batchId);
    
    CommitResultDTO commit(long batchId, String strategy, Long userId);
    
    List<BatchDTO> getBatches(Long userId, String moduleCode, int page, int size);
    
    BatchDTO getBatchDetail(long batchId);
    
    List<LogDTO> getBatchLogs(long batchId);
    
    byte[] exportErrors(long batchId);
    
    Map<String, Object> getBatchRows(long batchId, int page, int size, String status);
}
