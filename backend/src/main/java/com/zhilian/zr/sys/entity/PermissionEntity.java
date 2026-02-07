package com.zhilian.zr.sys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_permission")
public class PermissionEntity {
  @TableId
  private Long permId;
  private String permCode;
  private String permType;
  private String name;
  private String path;
  private String method;
  private Long parentId;
  private Integer sort;
  private Integer status;

  public Long getPermId() {
    return permId;
  }

  public void setPermId(Long permId) {
    this.permId = permId;
  }

  public String getPermCode() {
    return permCode;
  }

  public void setPermCode(String permCode) {
    this.permCode = permCode;
  }

  public String getPermType() {
    return permType;
  }

  public void setPermType(String permType) {
    this.permType = permType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
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
