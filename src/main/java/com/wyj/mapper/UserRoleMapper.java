package com.wyj.mapper;

import com.wyj.entity.UserRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserRoleMapper {
    @Delete({
        "delete from t_user_role",
        "where uid = #{uid,jdbcType=INTEGER}",
          "and role_id = #{roleId,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(UserRole key);

    @Insert({
        "insert into t_user_role (uid, role_id)",
        "values (#{uid,jdbcType=INTEGER}, #{roleId,jdbcType=INTEGER})"
    })
    int insert(UserRole record);

    int insertSelective(UserRole record);

    @Select("select * from t_user_role where uid=#{id}")
    @Results({@Result(column = "role_id",property = "role",one = @One(select = "com.wyj.mapper.RoleMapper.selectByPrimaryKey"))})
    List<UserRole> selectByUserId(int id);
}