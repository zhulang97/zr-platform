package com.zhilian.zr.importing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zr.importing.entity.ImportModuleConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ImportModuleConfigMapper extends BaseMapper<ImportModuleConfigEntity> {
    
    @Select("SELECT * FROM T_IMPORT_MODULE ORDER BY SORT_ORDER, MODULE_CODE")
    List<ImportModuleConfigEntity> selectAllOrdered();
    
    @Select("SELECT DISTINCT CATEGORY FROM T_IMPORT_MODULE WHERE CATEGORY IS NOT NULL ORDER BY CATEGORY")
    List<String> selectAllCategories();
}
