package com.zhilian.zr.sys.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDataScopeRelMapper {
  @Delete("delete from t_user_data_scope where user_id = #{userId}")
  int deleteByUserId(long userId);

  @Insert("insert into t_user_data_scope(user_id, district_id) values (#{userId}, #{districtId})")
  int insert(long userId, long districtId);
}
