package com.zhilian.zr.importing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zr.importing.entity.ImportModuleFieldEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ImportModuleFieldMapper extends BaseMapper<ImportModuleFieldEntity> {
    
    @Select("SELECT * FROM T_IMPORT_MODULE_FIELD WHERE MODULE_CODE = #{moduleCode} ORDER BY SORT_ORDER")
    List<ImportModuleFieldEntity> selectByModuleCode(String moduleCode);
}
