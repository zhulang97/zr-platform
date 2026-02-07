package com.zhilian.zr.dict.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_street")
public class StreetEntity {
  @TableId
  private Long id;
  private Long districtId;
  private String name;
  private String code;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getDistrictId() {
    return districtId;
  }

  public void setDistrictId(Long districtId) {
    this.districtId = districtId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
