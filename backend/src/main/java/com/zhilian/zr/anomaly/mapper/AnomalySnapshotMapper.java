package com.zhilian.zr.anomaly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zr.anomaly.entity.AnomalySnapshotEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnomalySnapshotMapper extends BaseMapper<AnomalySnapshotEntity> {

    @Select("select * from t_anomaly_snapshot where rule_id = #{ruleId} order by created_at desc limit 100")
    List<AnomalySnapshotEntity> listByRuleId(Long ruleId);

    @Select("select * from t_anomaly_snapshot where person_id = #{personId} order by created_at desc limit 100")
    List<AnomalySnapshotEntity> listByPersonId(Long personId);
}
