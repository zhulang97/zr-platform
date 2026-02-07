package com.zhilian.zr.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zr.sys.entity.RoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<RoleEntity> {

    @Select("select distinct r.* from t_role r join t_user_role ur on ur.role_id = r.role_id where ur.user_id = #{userId}")
    List<RoleEntity> listByUserId(long userId);

    @Select("select distinct r.* from t_role r join t_role_permission rp on rp.role_id = r.role_id where rp.perm_id = #{permId}")
    List<RoleEntity> listByPermissionId(long permId);
}
