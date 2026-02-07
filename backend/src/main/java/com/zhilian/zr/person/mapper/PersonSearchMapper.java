package com.zhilian.zr.person.mapper;

import com.zhilian.zr.person.dto.PersonDtos;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PersonSearchMapper {

  @Select("""
      <script>
      select count(distinct p.person_id)
      from t_person p
      left join t_disability_card dc on dc.person_id = p.person_id
      left join t_car_benefit cb on cb.person_id = p.person_id
      left join t_medical_benefit mb on mb.person_id = p.person_id
      left join t_pension_benefit pb on pb.person_id = p.person_id
      left join (select distinct person_id from t_anomaly where status = 'UNHANDLED') a on a.person_id = p.person_id
      where 1=1
        <if test='req.nameLike != null and req.nameLike != ""'>
          and p.name like '%' || #{req.nameLike} || '%'
        </if>
        <if test='req.idNo != null and req.idNo != ""'>
          and p.id_no = #{req.idNo}
        </if>
        <if test='req.districtIds != null and req.districtIds.size > 0'>
          and p.district_id in
          <foreach collection='req.districtIds' item='d' open='(' separator=',' close=')'>
            #{d}
          </foreach>
        </if>
        <if test='req.hasCar != null'>
          and cb.has_car = <choose><when test='req.hasCar'>1</when><otherwise>0</otherwise></choose>
        </if>
      </script>
      """)
  long count(@Param("req") PersonDtos.PersonSearchRequest req);

  @Select("""
      <script>
      select
        p.person_id as personId,
        p.name as name,
        p.id_no as idNo,
        dc.category_code as disabilityCategoryCode,
        cat.dict_name as disabilityCategoryName,
        dc.level_code as disabilityLevelCode,
        lvl.dict_name as disabilityLevelName,
        d.name as district,
        s.name as street,
        dc.status as cardStatusCode,
        cs.dict_name as cardStatusName,
        case when cb.has_car = 1 then 1 else 0 end as hasCar,
        case when mb.enabled = 1 then 1 else 0 end as hasMedicalSubsidy,
        case when pb.enabled = 1 then 1 else 0 end as hasPensionSubsidy,
        case when a.anomaly_id is not null then 1 else 0 end as riskFlag
      from t_person p
      left join t_disability_card dc on dc.person_id = p.person_id
      left join t_dict cat on cat.dict_type = 'disability_category' and cat.dict_code = dc.category_code and cat.status = 1
      left join t_dict lvl on lvl.dict_type = 'disability_level' and lvl.dict_code = dc.level_code and lvl.status = 1
      left join t_dict cs on cs.dict_type = 'card_status' and cs.dict_code = dc.status and cs.status = 1
      left join t_district d on d.id = p.district_id
      left join t_street s on s.id = p.street_id
      left join t_car_benefit cb on cb.person_id = p.person_id
      left join t_medical_benefit mb on mb.person_id = p.person_id
      left join t_pension_benefit pb on pb.person_id = p.person_id
      left join (select distinct anomaly_id, person_id from t_anomaly where status = 'UNHANDLED') a on a.person_id = p.person_id
      where 1=1
        <if test='req.nameLike != null and req.nameLike != ""'>
          and p.name like '%' || #{req.nameLike} || '%'
        </if>
        <if test='req.idNo != null and req.idNo != ""'>
          and p.id_no = #{req.idNo}
        </if>
        <if test='req.districtIds != null and req.districtIds.size > 0'>
          and p.district_id in
          <foreach collection='req.districtIds' item='d' open='(' separator=',' close=')'>
            #{d}
          </foreach>
        </if>
        <if test='req.hasCar != null'>
          and cb.has_car = <choose><when test='req.hasCar'>1</when><otherwise>0</otherwise></choose>
        </if>
      order by riskFlag desc, p.person_id desc
      offset #{offset} rows fetch next #{limit} rows only
      </script>
      """)
  List<PersonRowDb> list(@Param("req") PersonDtos.PersonSearchRequest req, @Param("limit") int limit, @Param("offset") int offset);

  @Select("""
      <script>
      select
        p.person_id as personId,
        p.name as name,
        p.id_no as idNo,
        dc.category_code as disabilityCategoryCode,
        cat.dict_name as disabilityCategoryName,
        dc.level_code as disabilityLevelCode,
        lvl.dict_name as disabilityLevelName,
        d.name as district,
        s.name as street,
        dc.status as cardStatusCode,
        cs.dict_name as cardStatusName,
        case when cb.has_car = 1 then 1 else 0 end as hasCar,
        case when mb.enabled = 1 then 1 else 0 end as hasMedicalSubsidy,
        case when pb.enabled = 1 then 1 else 0 end as hasPensionSubsidy,
        case when a.anomaly_id is not null then 1 else 0 end as riskFlag
      from t_person p
      left join t_disability_card dc on dc.person_id = p.person_id
      left join t_dict cat on cat.dict_type = 'disability_category' and cat.dict_code = dc.category_code and cat.status = 1
      left join t_dict lvl on lvl.dict_type = 'disability_level' and lvl.dict_code = dc.level_code and lvl.status = 1
      left join t_dict cs on cs.dict_type = 'card_status' and cs.dict_code = dc.status and cs.status = 1
      left join t_district d on d.id = p.district_id
      left join t_street s on s.id = p.street_id
      left join t_car_benefit cb on cb.person_id = p.person_id
      left join t_medical_benefit mb on mb.person_id = p.person_id
      left join t_pension_benefit pb on pb.person_id = p.person_id
      left join (select distinct anomaly_id, person_id from t_anomaly where status = 'UNHANDLED') a on a.person_id = p.person_id
      where 1=1
        <if test='req.nameLike != null and req.nameLike != ""'>
          and p.name like '%' || #{req.nameLike} || '%'
        </if>
        <if test='req.idNo != null and req.idNo != ""'>
          and p.id_no = #{req.idNo}
        </if>
        <if test='req.districtIds != null and req.districtIds.size > 0'>
          and p.district_id in
          <foreach collection='req.districtIds' item='d' open='(' separator=',' close=')'>
            #{d}
          </foreach>
        </if>
        <if test='req.hasCar != null'>
          and cb.has_car = <choose><when test='req.hasCar'>1</when><otherwise>0</otherwise></choose>
        </if>
      order by riskFlag desc, p.person_id desc
      fetch next #{limit} rows only
      </script>
      """)
  List<PersonRowDb> listForExport(@Param("req") PersonDtos.PersonSearchRequest req, @Param("limit") int limit);

  class PersonRowDb {
    private Long personId;
    private String name;
    private String idNo;
    private String disabilityCategoryCode;
    private String disabilityCategoryName;
    private String disabilityLevelCode;
    private String disabilityLevelName;
    private String district;
    private String street;
    private String cardStatusCode;
    private String cardStatusName;
    private Integer hasCar;
    private Integer hasMedicalSubsidy;
    private Integer hasPensionSubsidy;
    private Integer riskFlag;

    public Long getPersonId() {
      return personId;
    }

    public void setPersonId(Long personId) {
      this.personId = personId;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getIdNo() {
      return idNo;
    }

    public void setIdNo(String idNo) {
      this.idNo = idNo;
    }

    public String getDisabilityCategoryCode() {
      return disabilityCategoryCode;
    }

    public void setDisabilityCategoryCode(String disabilityCategoryCode) {
      this.disabilityCategoryCode = disabilityCategoryCode;
    }

    public String getDisabilityCategoryName() {
      return disabilityCategoryName;
    }

    public void setDisabilityCategoryName(String disabilityCategoryName) {
      this.disabilityCategoryName = disabilityCategoryName;
    }

    public String getDisabilityLevelCode() {
      return disabilityLevelCode;
    }

    public void setDisabilityLevelCode(String disabilityLevelCode) {
      this.disabilityLevelCode = disabilityLevelCode;
    }

    public String getDisabilityLevelName() {
      return disabilityLevelName;
    }

    public void setDisabilityLevelName(String disabilityLevelName) {
      this.disabilityLevelName = disabilityLevelName;
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

    public String getCardStatusCode() {
      return cardStatusCode;
    }

    public void setCardStatusCode(String cardStatusCode) {
      this.cardStatusCode = cardStatusCode;
    }

    public String getCardStatusName() {
      return cardStatusName;
    }

    public void setCardStatusName(String cardStatusName) {
      this.cardStatusName = cardStatusName;
    }

    public Integer getHasCar() {
      return hasCar;
    }

    public void setHasCar(Integer hasCar) {
      this.hasCar = hasCar;
    }

    public Integer getHasMedicalSubsidy() {
      return hasMedicalSubsidy;
    }

    public void setHasMedicalSubsidy(Integer hasMedicalSubsidy) {
      this.hasMedicalSubsidy = hasMedicalSubsidy;
    }

    public Integer getHasPensionSubsidy() {
      return hasPensionSubsidy;
    }

    public void setHasPensionSubsidy(Integer hasPensionSubsidy) {
      this.hasPensionSubsidy = hasPensionSubsidy;
    }

    public Integer getRiskFlag() {
      return riskFlag;
    }

    public void setRiskFlag(Integer riskFlag) {
      this.riskFlag = riskFlag;
    }
  }
}
