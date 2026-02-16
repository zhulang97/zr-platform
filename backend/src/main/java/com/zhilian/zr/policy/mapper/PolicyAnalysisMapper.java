package com.zhilian.zr.policy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zr.policy.entity.PolicyAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PolicyAnalysisMapper extends BaseMapper<PolicyAnalysis> {
    
    @Select("SELECT * FROM t_policy_analysis WHERE policy_id = #{policyId} ORDER BY version DESC")
    List<PolicyAnalysis> selectByPolicyId(@Param("policyId") Long policyId);
    
    @Select("SELECT * FROM t_policy_analysis WHERE policy_id = #{policyId} AND is_latest = 'Y'")
    PolicyAnalysis selectLatestByPolicyId(@Param("policyId") Long policyId);
}
