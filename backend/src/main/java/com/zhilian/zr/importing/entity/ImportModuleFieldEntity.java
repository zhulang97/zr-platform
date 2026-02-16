package com.zhilian.zr.importing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("T_IMPORT_MODULE_FIELD")
public class ImportModuleFieldEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String moduleCode;
    private String fieldCode;
    private String fieldName;
    private String dbColumn;
    private String dataType;
    private Integer isRequired;
    private Integer isUnique;
    private String defaultValue;
    private String validationRule;
    private Integer sortOrder;
}
