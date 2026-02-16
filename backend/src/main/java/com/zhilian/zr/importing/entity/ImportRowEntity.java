package com.zhilian.zr.importing.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("t_import_row")
public class ImportRowEntity {
    @TableId("row_id")
    private Long rowId;
    private Long batchId;
    private Integer rowNo;
    private String rawData;
    private String parsedData;
    private String mappedData;
    private String idCardValue;
    private String validateStatus;
    private String errorMsg;
    private Instant createdAt;

    public Long getRowId() {
        return rowId;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public String getParsedData() {
        return parsedData;
    }

    public void setParsedData(String parsedData) {
        this.parsedData = parsedData;
    }

    public String getMappedData() {
        return mappedData;
    }

    public void setMappedData(String mappedData) {
        this.mappedData = mappedData;
    }

    public String getIdCardValue() {
        return idCardValue;
    }

    public void setIdCardValue(String idCardValue) {
        this.idCardValue = idCardValue;
    }

    public String getValidateStatus() {
        return validateStatus;
    }

    public void setValidateStatus(String validateStatus) {
        this.validateStatus = validateStatus;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
