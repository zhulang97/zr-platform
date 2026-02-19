package com.zhilian.zr.person.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("T_FIELD_CLASSIFY")
public class FieldClassifyEntity {
    
    @TableId(value = "FIELD_CODE")
    private String fieldCode;
    
    private String fieldName;
    private String fieldCategory;
    private String mergeStrategy;
    private Integer displayOrder;
    
    public String getFieldCode() { return fieldCode; }
    public void setFieldCode(String fieldCode) { this.fieldCode = fieldCode; }
    
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    
    public String getFieldCategory() { return fieldCategory; }
    public void setFieldCategory(String fieldCategory) { this.fieldCategory = fieldCategory; }
    
    public String getMergeStrategy() { return mergeStrategy; }
    public void setMergeStrategy(String mergeStrategy) { this.mergeStrategy = mergeStrategy; }
    
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
