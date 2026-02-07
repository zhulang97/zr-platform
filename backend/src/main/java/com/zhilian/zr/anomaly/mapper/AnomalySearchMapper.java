package com.zhilian.zr.anomaly.mapper;

import com.zhilian.zr.anomaly.dto.AnomalyDtos;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AnomalySearchMapper {

  @Select("""
      <script>
      select count(1)
      from t_anomaly a
      join t_person p on p.person_id = a.person_id
      left join t_district d on d.id = p.district_id
      left join t_street s on s.id = p.street_id
      where 1=1
        <if test='req.anomalyType != null and req.anomalyType != ""'>
          and a.anomaly_type = #{req.anomalyType}
        </if>
        <if test='req.status != null and req.status != ""'>
          and a.status = #{req.status}
        </if>
        <if test='req.districtIds != null and req.districtIds.size > 0'>
          and p.district_id in
          <foreach collection='req.districtIds' item='d' open='(' separator=',' close=')'>
            #{d}
          </foreach>
        </if>
      </script>
      """)
  long count(@Param("req") AnomalyDtos.SearchRequest req);

  @Select("""
      <script>
      select
        a.anomaly_id as anomalyId,
        a.anomaly_type as anomalyType,
        at.dict_name as anomalyTypeName,
        a.status as status,
        a.hit_time as hitTime,
        p.name as personName,
        p.id_no as personIdNo,
        d.name as district,
        s.name as street
      from t_anomaly a
      join t_person p on p.person_id = a.person_id
      left join t_district d on d.id = p.district_id
      left join t_street s on s.id = p.street_id
      left join t_dict at on at.dict_type = 'anomaly_type' and at.dict_code = a.anomaly_type and at.status = 1
      where 1=1
        <if test='req.anomalyType != null and req.anomalyType != ""'>
          and a.anomaly_type = #{req.anomalyType}
        </if>
        <if test='req.status != null and req.status != ""'>
          and a.status = #{req.status}
        </if>
        <if test='req.districtIds != null and req.districtIds.size > 0'>
          and p.district_id in
          <foreach collection='req.districtIds' item='d' open='(' separator=',' close=')'>
            #{d}
          </foreach>
        </if>
      order by a.hit_time desc
      offset #{offset} rows fetch next #{limit} rows only
      </script>
      """)
  List<AnomalyRowDb> list(@Param("req") AnomalyDtos.SearchRequest req, @Param("limit") int limit, @Param("offset") int offset);

  @Select("""
      <script>
      select
        a.anomaly_id as anomalyId,
        a.anomaly_type as anomalyType,
        at.dict_name as anomalyTypeName,
        r.rule_code as ruleCode,
        r.name as ruleName,
        r.severity as severity,
        a.status as status,
        a.hit_time as hitTime,
        a.handle_note as handleNote,
        a.handled_at as handledAt,
        sn.snapshot_json as snapshotJson,
        p.name as personName,
        p.id_no as personIdNo,
        d.name as district,
        s.name as street
      from t_anomaly a
      join t_person p on p.person_id = a.person_id
      left join t_district d on d.id = p.district_id
      left join t_street s on s.id = p.street_id
      left join t_dict at on at.dict_type = 'anomaly_type' and at.dict_code = a.anomaly_type and at.status = 1
      left join t_rule r on r.rule_id = a.rule_id
      left join t_anomaly_snapshot sn on sn.anomaly_id = a.anomaly_id
      where a.anomaly_id = #{anomalyId}
        <if test='districtIds != null and districtIds.size > 0'>
          and p.district_id in
          <foreach collection='districtIds' item='di' open='(' separator=',' close=')'>
            #{di}
          </foreach>
        </if>
      </script>
      """)
  AnomalyDetailDb detail(@Param("anomalyId") long anomalyId, @Param("districtIds") List<Long> districtIds);

  @Select("""
      select a.anomaly_id
      from t_anomaly a
      join t_person p on p.person_id = a.person_id
      where a.anomaly_id = #{anomalyId}
        <if test='districtIds != null and districtIds.size > 0'>
          and p.district_id in
          <foreach collection='districtIds' item='d' open='(' separator=',' close=')'>
            #{d}
          </foreach>
        </if>
      """)
  Long existsInDistrictScope(@Param("anomalyId") long anomalyId, @Param("districtIds") List<Long> districtIds);

  class AnomalyRowDb {
    private Long anomalyId;
    private String anomalyType;
    private String anomalyTypeName;
    private String status;
    private java.time.Instant hitTime;
    private String personName;
    private String personIdNo;
    private String district;
    private String street;

    public Long getAnomalyId() {
      return anomalyId;
    }

    public void setAnomalyId(Long anomalyId) {
      this.anomalyId = anomalyId;
    }

    public String getAnomalyType() {
      return anomalyType;
    }

    public void setAnomalyType(String anomalyType) {
      this.anomalyType = anomalyType;
    }

    public String getAnomalyTypeName() {
      return anomalyTypeName;
    }

    public void setAnomalyTypeName(String anomalyTypeName) {
      this.anomalyTypeName = anomalyTypeName;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public java.time.Instant getHitTime() {
      return hitTime;
    }

    public void setHitTime(java.time.Instant hitTime) {
      this.hitTime = hitTime;
    }

    public String getPersonName() {
      return personName;
    }

    public void setPersonName(String personName) {
      this.personName = personName;
    }

    public String getPersonIdNo() {
      return personIdNo;
    }

    public void setPersonIdNo(String personIdNo) {
      this.personIdNo = personIdNo;
    }

    public String getDistrict() {
      return district;
    }

    public void setDistrict(String district) {
      this.district = district;
    }

    public String getStreet() {
      return street;
    }

    public void setStreet(String street) {
      this.street = street;
    }
  }

  class AnomalyDetailDb {
    private Long anomalyId;
    private String anomalyType;
    private String anomalyTypeName;
    private String ruleCode;
    private String ruleName;
    private String severity;
    private String status;
    private java.time.Instant hitTime;
    private String handleNote;
    private java.time.Instant handledAt;
    private String snapshotJson;
    private String personName;
    private String personIdNo;
    private String district;
    private String street;

    public Long getAnomalyId() {
      return anomalyId;
    }

    public void setAnomalyId(Long anomalyId) {
      this.anomalyId = anomalyId;
    }

    public String getAnomalyType() {
      return anomalyType;
    }

    public void setAnomalyType(String anomalyType) {
      this.anomalyType = anomalyType;
    }

    public String getAnomalyTypeName() {
      return anomalyTypeName;
    }

    public void setAnomalyTypeName(String anomalyTypeName) {
      this.anomalyTypeName = anomalyTypeName;
    }

    public String getRuleCode() {
      return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
      this.ruleCode = ruleCode;
    }

    public String getRuleName() {
      return ruleName;
    }

    public void setRuleName(String ruleName) {
      this.ruleName = ruleName;
    }

    public String getSeverity() {
      return severity;
    }

    public void setSeverity(String severity) {
      this.severity = severity;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public java.time.Instant getHitTime() {
      return hitTime;
    }

    public void setHitTime(java.time.Instant hitTime) {
      this.hitTime = hitTime;
    }

    public String getHandleNote() {
      return handleNote;
    }

    public void setHandleNote(String handleNote) {
      this.handleNote = handleNote;
    }

    public java.time.Instant getHandledAt() {
      return handledAt;
    }

    public void setHandledAt(java.time.Instant handledAt) {
      this.handledAt = handledAt;
    }

    public String getSnapshotJson() {
      return snapshotJson;
    }

    public void setSnapshotJson(String snapshotJson) {
      this.snapshotJson = snapshotJson;
    }

    public String getPersonName() {
      return personName;
    }

    public void setPersonName(String personName) {
      this.personName = personName;
    }

    public String getPersonIdNo() {
      return personIdNo;
    }

    public void setPersonIdNo(String personIdNo) {
      this.personIdNo = personIdNo;
    }

    public String getDistrict() {
      return district;
    }

    public void setDistrict(String district) {
      this.district = district;
    }

    public String getStreet() {
      return street;
    }

    public void setStreet(String street) {
      this.street = street;
    }
  }
}
