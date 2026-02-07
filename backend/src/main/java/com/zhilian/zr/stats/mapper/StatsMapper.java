package com.zhilian.zr.stats.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StatsMapper {

  @Select("""
      <script>
      select count(1)
      from t_person p
      where 1=1
        <if test='districtIds != null and districtIds.size > 0'>
          and p.district_id in
          <foreach collection='districtIds' item='d' open='(' separator=',' close=')'>
            #{d}
          </foreach>
        </if>
      </script>
      """)
  long countPersons(@Param("districtIds") List<Long> districtIds);

  @Select("""
      <script>
      select
        sum(case when cb.has_car = 1 then 1 else 0 end) as carCount,
        sum(case when mb.enabled = 1 then 1 else 0 end) as medicalCount,
        sum(case when pb.enabled = 1 then 1 else 0 end) as pensionCount,
        sum(case when bc.person_id is not null then 1 else 0 end) as blindCount
      from t_person p
      left join t_car_benefit cb on cb.person_id = p.person_id
      left join t_medical_benefit mb on mb.person_id = p.person_id
      left join t_pension_benefit pb on pb.person_id = p.person_id
      left join t_blind_card bc on bc.person_id = p.person_id
      where 1=1
        <if test='districtIds != null and districtIds.size > 0'>
          and p.district_id in
          <foreach collection='districtIds' item='d' open='(' separator=',' close=')'>
            #{d}
          </foreach>
        </if>
      </script>
      """)
  CoverageRow coverage(@Param("districtIds") List<Long> districtIds);

  @Select("""
      <script>
      select dc.category_code as k, count(distinct p.person_id) as v
      from t_person p
      left join t_disability_card dc on dc.person_id = p.person_id
      where 1=1
        <if test='districtIds != null and districtIds.size > 0'>
          and p.district_id in
          <foreach collection='districtIds' item='d' open='(' separator=',' close=')'>
            #{d}
          </foreach>
        </if>
      group by dc.category_code
      order by v desc
      </script>
      """)
  List<KvRow> disabilityCategoryDist(@Param("districtIds") List<Long> districtIds);

  @Select("""
      <script>
      select dc.level_code as k, count(distinct p.person_id) as v
      from t_person p
      left join t_disability_card dc on dc.person_id = p.person_id
      where 1=1
        <if test='districtIds != null and districtIds.size > 0'>
          and p.district_id in
          <foreach collection='districtIds' item='d' open='(' separator=',' close=')'>
            #{d}
          </foreach>
        </if>
      group by dc.level_code
      order by k asc
      </script>
      """)
  List<KvRow> disabilityLevelDist(@Param("districtIds") List<Long> districtIds);

  @Select("""
      <script>
      select d.name as k, count(1) as v
      from t_person p
      left join t_district d on d.id = p.district_id
      where 1=1
        <if test='districtIds != null and districtIds.size > 0'>
          and p.district_id in
          <foreach collection='districtIds' item='di' open='(' separator=',' close=')'>
            #{di}
          </foreach>
        </if>
      group by d.name
      order by v desc
      </script>
      """)
  List<KvRow> personsByDistrict(@Param("districtIds") List<Long> districtIds);

  class KvRow {
    private String k;
    private Long v;

    public String getK() {
      return k;
    }

    public void setK(String k) {
      this.k = k;
    }

    public Long getV() {
      return v;
    }

    public void setV(Long v) {
      this.v = v;
    }
  }

  class CoverageRow {
    private Long carCount;
    private Long medicalCount;
    private Long pensionCount;
    private Long blindCount;

    public Long getCarCount() {
      return carCount;
    }

    public void setCarCount(Long carCount) {
      this.carCount = carCount;
    }

    public Long getMedicalCount() {
      return medicalCount;
    }

    public void setMedicalCount(Long medicalCount) {
      this.medicalCount = medicalCount;
    }

    public Long getPensionCount() {
      return pensionCount;
    }

    public void setPensionCount(Long pensionCount) {
      this.pensionCount = pensionCount;
    }

    public Long getBlindCount() {
      return blindCount;
    }

    public void setBlindCount(Long blindCount) {
      this.blindCount = blindCount;
    }
  }
}
