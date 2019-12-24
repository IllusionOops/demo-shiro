package com.wyj.mapper;

import com.wyj.entity.RolePermission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RolePermissionMapper {
    @Delete({
            "delete from t_role_permission",
            "where permission_id = #{permissionId,jdbcType=INTEGER}",
            "and role_id = #{roleId,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(RolePermission key);

    @Insert({
            "insert into t_role_permission (permission_id, role_id)",
            "values (#{permissionId,jdbcType=INTEGER}, #{roleId,jdbcType=INTEGER})"
    })
    int insert(RolePermission record);

    int insertSelective(RolePermission record);

    @Select("select * from t_role_permission where role_id=#{roleId}")
    @Results({@Result(column = "permission_id", property = "permission", one = @One(select = "com.wyj.mapper.PermissionMapper.selectByPrimaryKey"))})
    List<RolePermission> selectByRoleId(int roleId);
}