package com.zhilian.zr.importing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zr.importing.entity.ImportRowEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ImportRowMapper extends BaseMapper<ImportRowEntity> {

    @Select("select * from t_import_row where batch_id = #{batchId}")
    List<ImportRowEntity> selectByBatchId(long batchId);

    @Insert("<script>insert into t_import_row(row_id, batch_id, row_no, data_json, validate_status) values <foreach collection='list' item='item' separator=','>(#{item.rowId}, #{item.batchId}, #{item.rowNo}, #{item.dataJson}, #{item.validateStatus})</foreach></script>")
    void insertBatch(List<ImportRowEntity> list);
}
