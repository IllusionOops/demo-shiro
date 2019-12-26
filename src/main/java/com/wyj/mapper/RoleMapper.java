package com.wyj.mapper;

import com.wyj.entity.Role;
import org.apache.ibatis.annotations.*;

@Mapper
public interface RoleMapper {
    @Delete({
        "delete from t_role",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
        "insert into t_role (id, description, ",
        "role)",
        "values (#{id,jdbcType=INTEGER}, #{description,jdbcType=VARCHAR}, ",
        "#{role,jdbcType=VARCHAR})"
    })
    int insert(Role record);

    int insertSelective(Role record);

    @Select({
        "select",
        "id, description, role",
        "from t_role",
        "where id = #{id,jdbcType=INTEGER}"
    })
    @Results({@Result(column = "id",property = "id",id = true),@Result(column = "id",property = "rolePermissionList",one = @One(select = "com.wyj.mapper.RolePermissionMapper.selectByRoleId"))})
    Role selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Role record);

    @Update({
        "update t_role",
        "set description = #{description,jdbcType=VARCHAR},",
          "role = #{role,jdbcType=VARCHAR}",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(Role record);
}