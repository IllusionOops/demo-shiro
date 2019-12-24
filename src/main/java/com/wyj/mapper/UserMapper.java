package com.wyj.mapper;

import com.wyj.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Delete({
        "delete from t_user",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
        "insert into t_user (id, username, ",
        "password, email,role_id)",
        "values (#{id,jdbcType=INTEGER}, #{username,jdbcType=VARCHAR}, ",
        "#{password,jdbcType=VARCHAR}, #{email,jdbcType=VARCHAR}, #{roleId,jdbcType=INTEGER})"
    })
    int insert(User record);

    int insertSelective(User record);

    @Select({
        "select",
        "id, username, password, email,role_id",
        "from t_user",
        "where id = #{id,jdbcType=INTEGER}"
    })
    User selectByPrimaryKey(Integer id);

    @Select({
            "select",
            "id, username, password, email,role_id",
            "from t_user",
            "where username = #{username,jdbcType=INTEGER}"
    })
    @Results({@Result(column = "id",property = "userRoleList",many = @Many(select = "com.wyj.mapper.UserRoleMapper.selectByUserId"))})
    User selectByUsername(String username);

    int updateByPrimaryKeySelective(User record);

    @Update({
        "update t_user",
        "set username = #{username,jdbcType=VARCHAR},",
          "password = #{password,jdbcType=VARCHAR},",
          "email = #{email,jdbcType=VARCHAR},",
          "role_id = #{roleId,jdbcType=INTEGER}",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(User record);
}