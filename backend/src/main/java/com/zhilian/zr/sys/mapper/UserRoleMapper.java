package com.zhilian.zr.sys.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRoleMapper {
  @Delete("delete from t_user_role where user_id = #{userId}")
  int deleteByUserId(long userId);

  @Insert("insert into t_user_role(user_id, role_id) values (#{userId}, #{roleId})")
  int insert(long userId, long roleId);
}
