package com.wyj.service;

import com.wyj.entity.*;
import com.wyj.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName UserService
 * @Description: TODO
 * @Author yjwu
 * @Date 2019/12/23 12:12
 **/
@Service
public class UserService {
    @Autowired
    private UserRoleMapper userRoleDAO;
    @Autowired
    private UserMapper userDAO;
    @Autowired
    private RoleMapper roleDAO;
    @Autowired
    private PermissionMapper permissionDAO;
    @Autowired
    private RolePermissionMapper rolePermissionDAO;

    //根据用户id查询所有的角色信息
    public List<Role> findRoles(Integer id) {
        List<Role> roleList=new ArrayList<>();
        List<UserRole> keyList =userRoleDAO.selectByUserId(id);
        List<Integer> roleIdList = new ArrayList<>(keyList.size());
        for (UserRole userRoleKey : keyList) {
            roleIdList.add(userRoleKey.getRoleId());
            roleList.add(userRoleKey.getRole());
        }
        return roleList;
    }
    //根据用户的id查询所有权限信息
    public List<Permission> findPermissions(Integer id) {
        List<Role> roles = findRoles(id);
        List<Permission> resultList = new ArrayList<>();
        List<RolePermission> rolePermissionKeys =new ArrayList<>();
        for (Role role : roles) {
            rolePermissionKeys.addAll(rolePermissionDAO.selectByRoleId(role.getId()));
        }
        for (RolePermission rolePermission:rolePermissionKeys){
            resultList.add(rolePermission.getPermission());
        }
        return resultList;
    }

    public User findUserById(String uId) {
        return userDAO.selectByPrimaryKey(Integer.valueOf(uId));
    }

    public User findUserByUsername(String username) {
        return userDAO.selectByUsername(username);
    }

    public User findUserRolePermissionByUsername(String username) {
        return userDAO.selectUserRolePermissionByUsername(username);
    }

}

