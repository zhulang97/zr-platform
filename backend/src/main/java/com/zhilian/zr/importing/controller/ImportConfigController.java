package com.zhilian.zr.importing.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.importing.entity.ImportModuleConfigEntity;
import com.zhilian.zr.importing.entity.ImportModuleFieldEntity;
import com.zhilian.zr.importing.mapper.ImportModuleConfigMapper;
import com.zhilian.zr.importing.mapper.ImportModuleFieldMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/import/config")
public class ImportConfigController {

    private final ImportModuleConfigMapper moduleMapper;
    private final ImportModuleFieldMapper fieldMapper;

    public ImportConfigController(ImportModuleConfigMapper moduleMapper, ImportModuleFieldMapper fieldMapper) {
        this.moduleMapper = moduleMapper;
        this.fieldMapper = fieldMapper;
    }

    @GetMapping("/modules")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ImportModuleConfigEntity>> listModules() {
        return ApiResponse.ok(moduleMapper.selectAllOrdered());
    }

    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<String>> listCategories() {
        return ApiResponse.ok(moduleMapper.selectAllCategories());
    }

    @GetMapping("/modules/{moduleCode}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ImportModuleConfigEntity> getModule(@PathVariable String moduleCode) {
        return ApiResponse.ok(moduleMapper.selectById(moduleCode));
    }

    @PostMapping("/modules")
    @PreAuthorize("hasAuthority('import:config:edit')")
    public ApiResponse<ImportModuleConfigEntity> createModule(@RequestBody ImportModuleConfigEntity module) {
        module.setCreatedAt(Instant.now());
        if (module.getIsActive() == null) module.setIsActive(1);
        if (module.getSortOrder() == null) module.setSortOrder(0);
        if (module.getIdCardField() == null) module.setIdCardField("ID_CARD");
        if (module.getCertNoField() == null) module.setCertNoField("DISABILITY_CERT_NO");
        moduleMapper.insert(module);
        return ApiResponse.ok(module);
    }

    @PutMapping("/modules/{moduleCode}")
    @PreAuthorize("hasAuthority('import:config:edit')")
    public ApiResponse<ImportModuleConfigEntity> updateModule(
            @PathVariable String moduleCode,
            @RequestBody ImportModuleConfigEntity module) {
        module.setModuleCode(moduleCode);
        moduleMapper.updateById(module);
        return ApiResponse.ok(module);
    }

    @DeleteMapping("/modules/{moduleCode}")
    @PreAuthorize("hasAuthority('import:config:delete')")
    public ApiResponse<Void> deleteModule(@PathVariable String moduleCode) {
        moduleMapper.deleteById(moduleCode);
        fieldMapper.delete(new LambdaQueryWrapper<ImportModuleFieldEntity>()
                .eq(ImportModuleFieldEntity::getModuleCode, moduleCode));
        return ApiResponse.ok(null);
    }

    @GetMapping("/modules/{moduleCode}/fields")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ImportModuleFieldEntity>> listFields(@PathVariable String moduleCode) {
        return ApiResponse.ok(fieldMapper.selectByModuleCode(moduleCode));
    }

    @PostMapping("/modules/{moduleCode}/fields")
    @PreAuthorize("hasAuthority('import:config:edit')")
    public ApiResponse<ImportModuleFieldEntity> createField(
            @PathVariable String moduleCode,
            @RequestBody ImportModuleFieldEntity field) {
        field.setModuleCode(moduleCode);
        if (field.getSortOrder() == null) field.setSortOrder(0);
        if (field.getIsRequired() == null) field.setIsRequired(0);
        if (field.getIsUnique() == null) field.setIsUnique(0);
        if (field.getDataType() == null) field.setDataType("STRING");
        fieldMapper.insert(field);
        return ApiResponse.ok(field);
    }

    @PutMapping("/fields/{id}")
    @PreAuthorize("hasAuthority('import:config:edit')")
    public ApiResponse<ImportModuleFieldEntity> updateField(
            @PathVariable Long id,
            @RequestBody ImportModuleFieldEntity field) {
        field.setId(id);
        fieldMapper.updateById(field);
        return ApiResponse.ok(field);
    }

    @DeleteMapping("/fields/{id}")
    @PreAuthorize("hasAuthority('import:config:delete')")
    public ApiResponse<Void> deleteField(@PathVariable Long id) {
        fieldMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/modules/{moduleCode}/fields/batch")
    @PreAuthorize("hasAuthority('import:config:edit')")
    public ApiResponse<List<ImportModuleFieldEntity>> batchCreateFields(
            @PathVariable String moduleCode,
            @RequestBody List<ImportModuleFieldEntity> fields) {
        for (ImportModuleFieldEntity field : fields) {
            field.setModuleCode(moduleCode);
            if (field.getSortOrder() == null) field.setSortOrder(0);
            if (field.getIsRequired() == null) field.setIsRequired(0);
            if (field.getIsUnique() == null) field.setIsUnique(0);
            if (field.getDataType() == null) field.setDataType("STRING");
            fieldMapper.insert(field);
        }
        return ApiResponse.ok(fields);
    }

    @PostMapping("/modules/sync-from-enum")
    @PreAuthorize("hasAuthority('import:config:edit')")
    public ApiResponse<Map<String, Integer>> syncFromEnum() {
        com.zhilian.zr.importing.enums.ImportModule[] enums = 
            com.zhilian.zr.importing.enums.ImportModule.values();
        int moduleCount = 0;
        int fieldCount = 0;
        
        for (com.zhilian.zr.importing.enums.ImportModule m : enums) {
            ImportModuleConfigEntity existing = moduleMapper.selectById(m.getCode());
            if (existing == null) {
                ImportModuleConfigEntity entity = new ImportModuleConfigEntity();
                entity.setModuleCode(m.getCode());
                entity.setModuleName(m.getName());
                entity.setTableName(m.getTableName());
                entity.setCategory(m.getCategory());
                entity.setIsActive(1);
                entity.setSortOrder(moduleCount);
                entity.setIdCardField("ID_CARD");
                entity.setCertNoField("DISABILITY_CERT_NO");
                entity.setCreatedAt(Instant.now());
                moduleMapper.insert(entity);
                moduleCount++;
            }
        }
        
        return ApiResponse.ok(Map.of("modules", moduleCount, "fields", fieldCount));
    }

    @PutMapping("/modules/{moduleCode}/fields/display")
    @PreAuthorize("hasAuthority('import:config:edit')")
    public ApiResponse<Void> updateFieldsDisplay(
            @PathVariable String moduleCode,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fields = (List<Map<String, Object>>) body.get("fields");
        
        if (fields != null) {
            for (Map<String, Object> f : fields) {
                Long id = Long.valueOf(f.get("id").toString());
                ImportModuleFieldEntity entity = fieldMapper.selectById(id);
                if (entity != null) {
                    entity.setIsDisplay(f.get("isDisplay") != null ? Integer.valueOf(f.get("isDisplay").toString()) : 1);
                    fieldMapper.updateById(entity);
                }
            }
        }
        
        return ApiResponse.ok(null);
    }
}
