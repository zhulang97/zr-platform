package com.zhilian.zr.importing.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.zhilian.zr.common.ids.IdGenerator;
import com.zhilian.zr.importing.entity.ImportBatchEntity;
import com.zhilian.zr.importing.entity.ImportRowEntity;
import com.zhilian.zr.importing.mapper.ImportBatchMapper;
import com.zhilian.zr.importing.mapper.ImportRowMapper;
import com.zhilian.zr.importing.service.ImportModuleService;
import com.zhilian.zr.importing.service.ImportService;
import com.zhilian.zr.person.mapper.PersonSearchMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportServiceImpl implements ImportService {

  private final ImportBatchMapper batchMapper;
  private final ImportRowMapper rowMapper;
  private final ImportModuleService importModuleService;

  public ImportServiceImpl(ImportBatchMapper batchMapper, ImportRowMapper rowMapper, ImportModuleService importModuleService) {
    this.batchMapper = batchMapper;
    this.rowMapper = rowMapper;
    this.importModuleService = importModuleService;
  }

  @Override
  public String generateTemplate(String type) {
    if ("person".equals(type)) {
      return "name,idNo,gender,birthday,districtId,streetId,address\n" +
             "张三,310115199001011234,男,1990-01-01,310115,310115001,上海市浦东新区\n";
    }
    return "template not available\n";
  }

  @Override
  public long upload(String type, MultipartFile file, String note) {
    long batchId = IdGenerator.nextId();
    ImportBatchEntity batch = new ImportBatchEntity();
    batch.setBatchId(batchId);
    batch.setModuleCode(type);
    batch.setFileName(file.getOriginalFilename());
    batch.setImportStrategy("ID_CARD_MERGE");
    batch.setStatus("UPLOADED");
    batch.setCreatedBy(1L); // TODO: get current user
    batch.setCreatedAt(Instant.now());
    batchMapper.insert(batch);

    try (InputStream is = file.getInputStream()) {
      AtomicInteger rowNo = new AtomicInteger(0);
      List<ImportRowEntity> rows = new ArrayList<>();

      EasyExcel.read(is, new AnalysisEventListener<Map<Integer, String>>() {
        @Override
        public void invoke(Map<Integer, String> data, AnalysisContext context) {
          int currentRow = rowNo.incrementAndGet();
          ImportRowEntity row = new ImportRowEntity();
          row.setRowId(IdGenerator.nextId());
          row.setBatchId(batchId);
          row.setRowNo(currentRow);
          row.setRawData(data.toString());
          row.setValidateStatus("PENDING");
          row.setCreatedAt(Instant.now());
          rows.add(row);

          if (rows.size() >= 100) {
            rowMapper.insertBatch(rows);
            rows.clear();
          }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
          if (!rows.isEmpty()) {
            rowMapper.insertBatch(rows);
          }
        }
      }).sheet().doRead();

    } catch (IOException e) {
      throw new RuntimeException("Failed to read file", e);
    }

    return batchId;
  }

  @Override
  public Map<String, Object> validate(long batchId) {
    // TODO: implement validation logic
    ImportBatchEntity batch = batchMapper.selectById(batchId);
    if (batch == null) {
      throw new IllegalArgumentException("Batch not found");
    }

    // Simple validation logic - check for duplicate idNo
    List<ImportRowEntity> rows = rowMapper.selectByBatchId(batchId);
    int total = rows.size();
    int errors = 0;
    List<String> errorMsgs = new ArrayList<>();

    for (ImportRowEntity row : rows) {
      // Basic validation: check if idNo or 证件号码 is present
      boolean hasIdCard = row.getRawData() != null && (
          row.getRawData().contains("idNo") || 
          row.getRawData().contains("证件号码") ||
          row.getRawData().contains("身份证号")
      );
      if (!hasIdCard) {
        errors++;
        errorMsgs.add("Row " + row.getRowNo() + ": missing idNo");
        row.setValidateStatus("ERROR");
        row.setErrorMsg("Missing idNo");
      } else {
        row.setValidateStatus("OK");
      }
      rowMapper.updateById(row);
    }

    return Map.of(
        "batchId", batchId,
        "total", total,
        "errors", errors,
        "status", errors == 0 ? "VALID" : "INVALID",
        "errorMessages", errorMsgs
    );
  }

  @Override
  public Map<String, Object> commit(long batchId, String strategy) {
    ImportBatchEntity batch = batchMapper.selectById(batchId);
    if (batch == null) {
      throw new IllegalArgumentException("Batch not found");
    }

    // 调用 ImportModuleService 来执行实际的导入逻辑
    try {
      var result = importModuleService.commit(batchId, strategy, null);
      return Map.of(
          "batchId", batchId,
          "strategy", strategy,
          "total", result.totalRows(),
          "success", result.successRows(),
          "failed", result.failedRows()
      );
    } catch (Exception e) {
      throw new RuntimeException("导入失败: " + e.getMessage(), e);
    }
  }
}