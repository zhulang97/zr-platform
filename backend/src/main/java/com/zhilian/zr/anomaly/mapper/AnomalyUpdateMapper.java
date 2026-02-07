package com.zhilian.zr.anomaly.mapper;

import java.time.Instant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AnomalyUpdateMapper {

  @Update("""
      update t_anomaly
      set status = #{status}, handle_note = #{note}, handler_user_id = #{handlerUserId}, handled_at = #{handledAt}
      where anomaly_id = #{anomalyId}
      """)
  int updateStatus(@Param("anomalyId") long anomalyId,
      @Param("status") String status,
      @Param("note") String note,
      @Param("handlerUserId") long handlerUserId,
      @Param("handledAt") Instant handledAt);
}
