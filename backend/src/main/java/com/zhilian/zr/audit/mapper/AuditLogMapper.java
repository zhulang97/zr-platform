package com.zhilian.zr.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zr.audit.entity.AuditLogEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.Instant;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogEntity> {

    @Insert("""
        insert into t_audit_log(id, user_id, action, resource, request_clob, result_code, cost_ms, created_at, ip, ua)
        values (#{id}, #{userId}, #{action}, #{resource}, #{requestClob}, #{resultCode}, #{costMs}, #{createdAt}, #{ip}, #{ua})
        """)
    int insert(@Param("id") long id,
        @Param("userId") Long userId,
        @Param("action") String action,
        @Param("resource") String resource,
        @Param("requestClob") String requestClob,
        @Param("resultCode") String resultCode,
        @Param("costMs") Long costMs,
        @Param("createdAt") Instant createdAt,
        @Param("ip") String ip,
        @Param("ua") String ua);

    @Select("select * from t_audit_log where user_id = #{userId} order by created_at desc limit 1000")
    java.util.List<AuditLogEntity> listByUserId(Long userId);

    @Select("select * from t_audit_log where action like #{actionLike} order by created_at desc limit 1000")
    java.util.List<AuditLogEntity> listByAction(String actionLike);

    @Select("select * from t_audit_log where resource like #{resourceLike} order by created_at desc limit 1000")
    java.util.List<AuditLogEntity> listByResource(String resourceLike);
}
