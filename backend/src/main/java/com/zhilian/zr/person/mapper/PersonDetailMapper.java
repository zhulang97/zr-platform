package com.zhilian.zr.person.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PersonDetailMapper {

  @Select("""
      <script>
      select
        p.person_id as personId,
        p.name as name,
        p.id_no as idNo,
        dc.card_no as disabilityCardNo,
        dc.category_code as disabilityCategoryCode,
        cat.dict_name as disabilityCategoryName,
        dc.level_code as disabilityLevelCode,
        lvl.dict_name as disabilityLevelName,
        dc.issue_date as issueDate,
        dc.status as cardStatusCode,
        cs.dict_name as cardStatusName
      from t_person p
      left join t_disability_card dc on dc.person_id = p.person_id
      left join t_dict cat on cat.dict_type = 'disability_category' and cat.dict_code = dc.category_code and cat.status = 1
      left join t_dict lvl on lvl.dict_type = 'disability_level' and lvl.dict_code = dc.level_code and lvl.status = 1
      left join t_dict cs on cs.dict_type = 'card_status' and cs.dict_code = dc.status and cs.status = 1
      where p.person_id = #{personId}
        <if test='districtIds != null and districtIds.size > 0'>
          and p.district_id in
          <foreach collection='districtIds' item='d' open='(' separator=',' close=')'>
            #{d}
          </foreach>
        </if>
      </script>
      """)
  Map<String, Object> detail(@Param("personId") long personId, @Param("districtIds") List<Long> districtIds);

  @Select("""
      <script>
      select
        cb.has_car as hasCar,
        cb.plate_no as plateNo,
        cb.annual_inspection_status as annualInspectionStatus,
        cb.car_owner_id_no as carOwnerIdNo,
        case when cb.has_car = 1 and cb.car_owner_id_no is not null and cb.car_owner_id_no <> p.id_no then 1 else 0 end as personCarSeparated,
        mb.enabled as medicalEnabled,
        mb.status as medicalStatus,
        mb.last_pay_date as medicalLastPayDate,
        pb.enabled as pensionEnabled,
        pb.status as pensionStatus,
        pb.last_pay_date as pensionLastPayDate,
        bc.card_no as blindCardNo,
        bc.status as blindStatus
      from t_person p
      left join t_car_benefit cb on cb.person_id = p.person_id
      left join t_medical_benefit mb on mb.person_id = p.person_id
      left join t_pension_benefit pb on pb.person_id = p.person_id
      left join t_blind_card bc on bc.person_id = p.person_id
      where p.person_id = #{personId}
        <if test='districtIds != null and districtIds.size > 0'>
          and p.district_id in
          <foreach collection='districtIds' item='d' open='(' separator=',' close=')'>
            #{d}
          </foreach>
        </if>
      </script>
      """)
  Map<String, Object> biz(@Param("personId") long personId, @Param("districtIds") List<Long> districtIds);

  @Select("""
      <script>
      select a.anomaly_id as anomalyId, a.anomaly_type as anomalyType, at.dict_name as anomalyTypeName, a.status as status, a.hit_time as hitTime
      from t_anomaly a
      join t_person p on p.person_id = a.person_id
      left join t_dict at on at.dict_type = 'anomaly_type' and at.dict_code = a.anomaly_type and at.status = 1
      where a.person_id = #{personId}
        <if test='districtIds != null and districtIds.size > 0'>
          and p.district_id in
          <foreach collection='districtIds' item='d' open='(' separator=',' close=')'>
            #{d}
          </foreach>
        </if>
      order by a.hit_time desc
      </script>
      """)
  List<Map<String, Object>> risks(@Param("personId") long personId, @Param("districtIds") List<Long> districtIds);
}
