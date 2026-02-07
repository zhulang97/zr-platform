package com.zhilian.zr.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zr.dict.entity.DictEntryEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DictEntryMapper extends BaseMapper<DictEntryEntity> {

    @Select("""
        select dict_type as dictType, dict_code as dictCode, dict_name as dictName, sort, status
        from t_dict
        where dict_type = #{dictType} and status = 1
        order by sort asc
        """)
    List<DictEntryEntity> listByType(String dictType);

    @Select("""
        select distinct dict_type as dictType, dict_type as dictCode, dict_type as dictName, 0 as sort, 1 as status
        from t_dict
        order by dict_type asc
        """)
    List<DictEntryEntity> listTypes();

    @Select("select * from t_dict where dict_type = #{dictType} order by sort asc")
    List<DictEntryEntity> listItemsByType(String dictType);
}
