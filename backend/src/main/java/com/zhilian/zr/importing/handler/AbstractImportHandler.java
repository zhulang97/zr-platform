package com.zhilian.zr.importing.handler;

import com.zhilian.zr.importing.enums.ImportModule;
import com.zhilian.zr.importing.enums.ImportStrategy;

import java.util.List;
import java.util.Map;

public abstract class AbstractImportHandler {

    protected final ImportModule module;

    protected AbstractImportHandler(ImportModule module) {
        this.module = module;
    }

    public ImportModule getModule() {
        return module;
    }

    public String getTableName() {
        return module.getTableName();
    }

    public abstract int backup();
    
    public abstract int restore(long backupId);

    public abstract ImportResult importData(
        List<Map<String, String>> rows,
        ImportStrategy strategy,
        ImportContext context
    );

    public abstract List<ValidationError> validate(Map<String, String> row);

    protected String extractIdCard(Map<String, String> row) {
        for (Map.Entry<String, String> e : row.entrySet()) {
            String key = e.getKey();
            if (key.contains("证件") || key.contains("身份证")) {
                String val = e.getValue();
                if (val != null && val.length() >= 15) {
                    if (val.length() == 20) {
                        return val.substring(0, 18);
                    }
                    return val;
                }
            }
        }
        return null;
    }

    protected String normalizeValue(String value) {
        if (value == null) return null;
        return value.trim();
    }

    public record ImportResult(
        int total,
        int inserted,
        int updated,
        int skipped,
        int failed,
        List<String> errors
    ) {
        public static ImportResult empty() {
            return new ImportResult(0, 0, 0, 0, 0, List.of());
        }
    }

    public record ValidationError(
        int rowNo,
        String field,
        String message,
        String value
    ) {}

    public record ImportContext(
        long batchId,
        long userId,
        long backupId,
        boolean dryRun
    ) {}
}
