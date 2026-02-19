package com.zhilian.zr.policy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zr.policy.entity.PolicyDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PolicyMapper extends BaseMapper<PolicyDocument> {
    
    @Select("SELECT p.* FROM t_policy_document p " +
            "WHERE p.user_id = #{userId} AND p.status = 'ACTIVE' " +
            "ORDER BY p.created_at DESC")
    List<PolicyDocument> selectListWithLatestVersion(@Param("userId") Long userId);
    
    @Select("SELECT COALESCE(MAX(version), 0) + 1 FROM t_policy_analysis WHERE policy_id = #{policyId}")
    Integer selectNextVersion(@Param("policyId") Long policyId);
    
    @Select("UPDATE t_policy_analysis SET is_latest = 'N' WHERE policy_id = #{policyId}")
    void clearLatestVersion(@Param("policyId") Long policyId);
}
