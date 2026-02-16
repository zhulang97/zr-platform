package com.zhilian.zr.importing.dto;

import java.util.List;
import java.util.Map;

public class ImportDtos {

    public record ModuleDTO(
        String code,
        String name,
        String category,
        String tableName,
        int fieldCount,
        boolean hasPermission
    ) {}

    public record ModuleFieldDTO(
        String fieldCode,
        String fieldName,
        String dataType,
        boolean required,
        boolean unique,
        String defaultValue,
        String validationRule
    ) {}

    public record UploadResultDTO(
        long batchId,
        String moduleCode,
        String fileName,
        int totalRows,
        List<String> columns,
        List<FieldMappingDTO> suggestedMappings
    ) {}

    public record FieldMappingDTO(
        String excelColumn,
        String systemField,
        String fieldName,
        boolean matched,
        double confidence,
        List<String> sampleValues
    ) {}

    public record MappingPreviewDTO(
        long batchId,
        List<FieldMappingDTO> mappings,
        List<ModuleFieldDTO> systemFields,
        Map<String, String> fieldMapping,
        List<String> missingRequired,
        List<String> extraColumns
    ) {}

    public record ValidateResultDTO(
        long batchId,
        int totalRows,
        int validRows,
        int errorRows,
        List<RowErrorDTO> errors
    ) {}

    public record RowErrorDTO(
        int rowNo,
        String field,
        String message,
        String value
    ) {}

    public record CommitResultDTO(
        long batchId,
        String moduleCode,
        String strategy,
        int totalRows,
        int successRows,
        int failedRows,
        int insertedRows,
        int updatedRows,
        int backupRows
    ) {}

    public record BatchDTO(
        long batchId,
        String moduleCode,
        String moduleName,
        String fileName,
        String status,
        String strategy,
        int totalRows,
        int successRows,
        int failedRows,
        String createdBy,
        String createdAt,
        String committedAt
    ) {}

    public record LogDTO(
        long id,
        String logType,
        Integer rowNo,
        String fieldCode,
        String message,
        String createdAt
    ) {}
}
