package com.zhilian.zr.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zr.sys.entity.PermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<PermissionEntity> {

  @Select("""
      select distinct p.*
      from t_permission p
      join t_role_permission rp on rp.perm_id = p.perm_id
      join t_user_role ur on ur.role_id = rp.role_id
      where ur.user_id = #{userId} and p.status = 1
      """)
  List<PermissionEntity> listByUserId(long userId);

  @Select("""
      select distinct p.perm_code
      from t_permission p
      join t_role_permission rp on rp.perm_id = p.perm_id
      join t_user_role ur on ur.role_id = rp.role_id
      where ur.user_id = #{userId} and p.status = 1
      """)
  List<String> listCodesByUserId(long userId);
}
