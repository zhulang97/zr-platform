package com.zhilian.zr.sys.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserDataScopeMapper {
  @Select("select district_id from t_user_data_scope where user_id = #{userId}")
  List<Long> listDistrictIds(long userId);
}
