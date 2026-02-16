package com.zhilian.zr.importing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zr.importing.entity.ImportRowEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ImportRowMapper extends BaseMapper<ImportRowEntity> {

    @Select("SELECT * FROM T_IMPORT_ROW WHERE BATCH_ID = #{batchId} ORDER BY ROW_NO")
    List<ImportRowEntity> selectByBatchId(long batchId);

    @Insert("<script>INSERT INTO T_IMPORT_ROW(ROW_ID, BATCH_ID, ROW_NO, RAW_DATA, PARSED_DATA, MAPPED_DATA, ID_CARD_VALUE, VALIDATE_STATUS, ERROR_MSG, CREATED_AT) VALUES <foreach collection='list' item='item' separator=','>(#{item.rowId}, #{item.batchId}, #{item.rowNo}, #{item.rawData}, #{item.parsedData}, #{item.mappedData}, #{item.idCardValue}, #{item.validateStatus}, #{item.errorMsg}, #{item.createdAt})</foreach></script>")
    void insertBatch(List<ImportRowEntity> list);
}
