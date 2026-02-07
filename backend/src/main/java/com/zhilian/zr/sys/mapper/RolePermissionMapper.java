package com.zhilian.zr.sys.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RolePermissionMapper {

    @Delete("delete from t_role_permission where role_id = #{roleId}")
    int deleteByRoleId(long roleId);

    @Delete("delete from t_role_permission where perm_id = #{permId}")
    int deleteByPermissionId(long permId);

    @Insert("insert into t_role_permission(role_id, perm_id) values (#{roleId}, #{permId})")
    int insert(long roleId, long permId);

    @Select("select perm_id from t_role_permission where role_id = #{roleId}")
    List<Long> listPermissionIdsByRoleId(long roleId);
}
