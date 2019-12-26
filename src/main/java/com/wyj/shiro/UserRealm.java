package com.wyj.shiro;

import com.wyj.entity.Role;
import com.wyj.entity.RolePermission;
import com.wyj.entity.User;
import com.wyj.entity.UserRole;
import com.wyj.service.UserService;
import com.wyj.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName UserRealm
 * @Description: TODO
 * @Author yjwu
 * @Date 2019/12/24 10:52
 **/
@Slf4j
public class UserRealm extends AuthorizingRealm {
    @Autowired
    UserService userService;
    private static final String encryptSalt = "F12839WhsnnEV$#23b";
    @Autowired
    @Qualifier("strRedisTemplate")
    private RedisTemplate redisTemplate;

    /**
     * 必须重写此方法，不然会报错
     */
    @Override
    public boolean supports(AuthenticationToken token) {
//        return token instanceof UsernamePasswordToken;
        return token instanceof JWTToken;
    }

    /**
     * 默认使用此方法进行用户名正确与否验证，错误抛出异常即可。
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        System.out.println("————身份认证开始————");
//        UsernamePasswordToken userpasswordToken = (UsernamePasswordToken)authenticationToken;
//        String username = userpasswordToken.getUsername();
//        User user = userService.findUserByUsername(username);
//        if(user == null){
//            throw new AuthenticationException("用户名或者密码错误");
//        }
//        return new SimpleAuthenticationInfo(user, user.getPassword(), ByteSource.Util.bytes(encryptSalt), getName());

        String token = (String) authenticationToken.getCredentials();
        // 解密获得username，用于和数据库进行对比
        String username = JWTUtil.getUsername(token);
        if (username == null || !JWTUtil.verify(token, username)) {
            throw new AuthenticationException("token认证失败！");
        }
        User userByUsername = userService.findUserByUsername(username);
        if (userByUsername == null) {
            throw new AuthenticationException("该用户不存在！");
        }

        return new SimpleAuthenticationInfo(token, token, getName());
    }

    /**
     * 只有当需要检测用户权限的时候才会调用此方法，例如checkRole,checkPermission之类的
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        System.out.println("————权限认证开始————");
        Subject subject = SecurityUtils.getSubject();
        subject.getSession(true);
        String principal = (String) subject.getPrincipal();
        String username = JWTUtil.getUsername(principals.toString());
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        // 此处最好使用缓存提升速度
        User userByUsername = userService.findUserRolePermissionByUsername(username);
        if (userByUsername == null || userByUsername.getUserRoleList().isEmpty()) {
            return authorizationInfo;
        }
        //user--userrolelist--role--rolepermissionlist--permission
        for (UserRole userRole : userByUsername.getUserRoleList()) {
            Role role = userRole.getRole();

            if (role != null || !role.getRolePermissionList().isEmpty()) {
                authorizationInfo.addRole(role.getRole());
            } else {
                continue;
            }

            for (RolePermission rolePermission : role.getRolePermissionList()) {
                if (rolePermission.getPermission() != null && rolePermission.getPermission() != null) {
                    authorizationInfo.addStringPermission(rolePermission.getPermission().getName());
                } else {
                    continue;
                }
            }
        }
        return authorizationInfo;
    }
}

