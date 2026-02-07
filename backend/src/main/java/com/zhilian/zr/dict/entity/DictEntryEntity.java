package com.zhilian.zr.dict.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_dict")
public class DictEntryEntity {
  // MyBatis-Plus requires a @TableId field; use a synthetic composite id string.
  @TableId
  private String id;

  private String dictType;
  private String dictCode;
  private String dictName;
  private Integer sort;
  private Integer status;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDictType() {
    return dictType;
  }

  public void setDictType(String dictType) {
    this.dictType = dictType;
  }

  public String getDictCode() {
    return dictCode;
  }

  public void setDictCode(String dictCode) {
    this.dictCode = dictCode;
  }

  public String getDictName() {
    return dictName;
  }

  public void setDictName(String dictName) {
    this.dictName = dictName;
  }

  public Integer getSort() {
    return sort;
  }

  public void setSort(Integer sort) {
    this.sort = sort;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }
}
