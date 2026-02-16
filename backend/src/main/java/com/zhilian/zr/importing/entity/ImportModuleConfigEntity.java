package com.zhilian.zr.importing.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.Instant;

@Data
@TableName("T_IMPORT_MODULE")
public class ImportModuleConfigEntity {
    @TableId
    private String moduleCode;
    private String moduleName;
    private String tableName;
    private String category;
    private String idCardField;
    private String certNoField;
    private Integer sortOrder;
    private Integer isActive;
    private String permissionCode;
    private Instant createdAt;
}
