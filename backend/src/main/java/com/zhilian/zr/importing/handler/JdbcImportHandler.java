package com.zhilian.zr.importing.handler;

import com.zhilian.zr.importing.enums.ImportModule;
import com.zhilian.zr.importing.enums.ImportStrategy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

public class JdbcImportHandler extends AbstractImportHandler {

    private final JdbcTemplate jdbcTemplate;
    private final List<String> fieldNames;

    public JdbcImportHandler(ImportModule module, JdbcTemplate jdbcTemplate) {
        super(module);
        this.jdbcTemplate = jdbcTemplate;
        this.fieldNames = module.getFields();
    }

    @Override
    @Transactional
    public int backup() {
        String backupTable = getTableName() + "_BACKUP_" + System.currentTimeMillis();
        String sql = String.format("CREATE TABLE %s AS SELECT * FROM %s", backupTable, getTableName());
        jdbcTemplate.execute(sql);
        
        String countSql = "SELECT COUNT(*) FROM " + backupTable;
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    @Transactional
    public int restore(long backupId) {
        return 0;
    }

    @Override
    @Transactional
    public ImportResult importData(List<Map<String, String>> rows, ImportStrategy strategy, ImportContext context) {
        if (rows.isEmpty()) {
            return ImportResult.empty();
        }

        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        if (strategy == ImportStrategy.FULL_REPLACE) {
            jdbcTemplate.update("DELETE FROM " + getTableName());
        }

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            int rowNo = i + 1;

            try {
                List<ValidationError> validationErrors = validate(row);
                if (!validationErrors.isEmpty()) {
                    failed++;
                    errors.add(String.format("Row %d: %s", rowNo, 
                        validationErrors.stream()
                            .map(ValidationError::message)
                            .collect(Collectors.joining("; "))));
                    continue;
                }

                String idCard = extractIdCard(row);
                if (idCard == null) {
                    failed++;
                    errors.add(String.format("Row %d: Cannot extract ID card", rowNo));
                    continue;
                }

                if (strategy == ImportStrategy.ID_CARD_MERGE) {
                    boolean exists = checkExists(idCard);
                    if (exists) {
                        updateRow(row, idCard);
                        updated++;
                    } else {
                        insertRow(row);
                        inserted++;
                    }
                } else {
                    insertRow(row);
                    inserted++;
                }
            } catch (Exception e) {
                failed++;
                errors.add(String.format("Row %d: %s", rowNo, e.getMessage()));
            }
        }

        return new ImportResult(rows.size(), inserted, updated, skipped, failed, errors);
    }

    @Override
    public List<ValidationError> validate(Map<String, String> row) {
        List<ValidationError> errors = new ArrayList<>();

        for (String fieldName : fieldNames) {
            boolean required = fieldName.contains("姓名") || 
                             fieldName.contains("证件") || 
                             fieldName.contains("身份证");
            
            if (required) {
                boolean found = row.keySet().stream()
                    .anyMatch(k -> k.equals(fieldName) || k.contains(fieldName));
                
                if (!found || row.values().stream()
                    .filter(Objects::nonNull)
                    .noneMatch(v -> row.entrySet().stream()
                        .filter(e -> e.getKey().contains(fieldName))
                        .anyMatch(e -> e.getValue() != null && !e.getValue().trim().isEmpty()))) {
                    
                    String value = row.entrySet().stream()
                        .filter(e -> e.getKey().contains(fieldName))
                        .map(Map.Entry::getValue)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse("");
                    
                    if (value.trim().isEmpty()) {
                        errors.add(new ValidationError(0, fieldName, fieldName + "不能为空", value));
                    }
                }
            }
        }

        String idCard = extractIdCard(row);
        if (idCard != null && idCard.length() != 18 && idCard.length() != 15) {
            errors.add(new ValidationError(0, "证件号", "证件号格式不正确", idCard));
        }

        return errors;
    }

    private boolean checkExists(String idCard) {
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE ID_CARD = ?", getTableName());
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idCard);
        return count != null && count > 0;
    }

    private void insertRow(Map<String, String> row) {
        String idCard = extractIdCard(row);
        
        List<String> columns = new ArrayList<>();
        columns.add("ID_CARD");
        List<String> placeholders = new ArrayList<>();
        placeholders.add("?");
        List<Object> values = new ArrayList<>();
        values.add(idCard);

        for (Map.Entry<String, String> e : row.entrySet()) {
            String col = toColumnName(e.getKey());
            if (!columns.contains(col) && e.getValue() != null) {
                columns.add(col);
                placeholders.add("?");
                values.add(normalizeValue(e.getValue()));
            }
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
            getTableName(),
            String.join(", ", columns),
            String.join(", ", placeholders));

        jdbcTemplate.update(sql, values.toArray());
    }

    private void updateRow(Map<String, String> row, String idCard) {
        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Map.Entry<String, String> e : row.entrySet()) {
            String col = toColumnName(e.getKey());
            if (!"ID_CARD".equals(col) && e.getValue() != null) {
                setClauses.add(col + " = ?");
                values.add(normalizeValue(e.getValue()));
            }
        }

        if (setClauses.isEmpty()) {
            return;
        }

        values.add(idCard);
        String sql = String.format("UPDATE %s SET %s WHERE ID_CARD = ?",
            getTableName(),
            String.join(", ", setClauses));

        jdbcTemplate.update(sql, values.toArray());
    }

    private String toColumnName(String fieldName) {
        String code = fieldName.replaceAll("[/（）()\\s]+", "_")
            .replaceAll("（", "_")
            .replaceAll("）", "")
            .toUpperCase();
        
        Map<String, String> specialMapping = new HashMap<>();
        specialMapping.put("姓名", "NAME");
        specialMapping.put("证件号码", "ID_CARD");
        specialMapping.put("证件号", "ID_CARD");
        specialMapping.put("身份证号", "ID_CARD");
        specialMapping.put("身份证", "ID_CARD");
        specialMapping.put("性别", "GENDER");
        specialMapping.put("年龄", "AGE");
        specialMapping.put("联系电话", "PHONE");
        specialMapping.put("联系地址", "ADDRESS");
        
        return specialMapping.getOrDefault(fieldName, code);
    }
}
