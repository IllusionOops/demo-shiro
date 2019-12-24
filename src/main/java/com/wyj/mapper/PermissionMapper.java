package com.wyj.mapper;

import com.wyj.entity.Permission;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PermissionMapper {
    @Delete({
        "delete from t_permission",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
        "insert into t_permission (id, name, ",
        "description)",
        "values (#{id,jdbcType=INTEGER}, #{name,jdbcType=VARCHAR}, ",
        "#{description,jdbcType=VARCHAR})"
    })
    int insert(Permission record);

    int insertSelective(Permission record);

    @Select({
        "select",
        "id, name, description",
        "from t_permission",
        "where id = #{id,jdbcType=INTEGER}"
    })
    Permission selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Permission record);

    @Update({
        "update t_permission",
        "set name = #{name,jdbcType=VARCHAR},",
          "description = #{description,jdbcType=VARCHAR}",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(Permission record);
}