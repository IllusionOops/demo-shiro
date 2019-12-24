package com.wyj.config;

import com.wyj.entity.Permission;
import com.wyj.entity.Role;
import com.wyj.entity.User;
import com.wyj.service.UserService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ClassName UserRealm
 * @Description: TODO 获取两处信息。Subject即用户传过来的信息，提供给shiro的userService接口以获取权限信息和角色信息
 * 首先，UserRealm这个类继承了AuthorizingRealm，这个类的作用是两处获取信息，一处是Subject即用户传过来的信息；一处是我们提供给shiro的userService接口以获取权限信息和角色信息。拿这两个信息之后AuthorizingRealm会自动进行比较，判断用户名密码，用户权限等等。
 * 然后，拿用户凭证信息的是doGetAuthenticationInfo接口，拿角色权限信息的是doGetAuthorizationInfo接口
 * 然后，两个重要参数，AuthenticationToken是我们可以自己实现的用户凭证/密钥信息，PrincipalCollection是用户凭证信息集合。注意Principals表示凭证（比如用户名、手机号、邮箱等）
 * 最后，配置完成对比的两方之后Subject.login(token)的时候就会调用doGetAuthenticationInfo方法；涉及到Subject.hasRole或者Subject.hasPermission的时候就会调用doGetAuthorizationInfo方法；
 * @Author yjwu
 * @Date 2019/12/23 14:00
 **/
@Component
public class UserRealm11 extends AuthorizingRealm {
    @Autowired
    private UserService userService;
    /**
     * @MethodName: doGetAuthorizationInfo
     * @Description: TODO 拿角色权限信息
     * @Param:  * @param principalCollection 是用户凭证信息集合。注意Principals表示凭证（比如用户名、手机号、邮箱等）
     * @Return: {@link AuthorizationInfo}
     * @Author: yjwu
     * @Date: 2019/12/23 14:02
     **/
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        String uid = (String) principalCollection.getPrimaryPrincipal();
        List<Role> roles = userService.findRoles(Integer.valueOf(uid));
        Set<String> roleNames = new HashSet<>(roles.size());
        for (Role role : roles) {
            roleNames.add(role.getRole());
        }
        //此处把当前subject对应的所有角色信息交给shiro，调用hasRole的时候就根据这些role信息判断
        authorizationInfo.setRoles(roleNames);
        List<Permission> permissions = userService.findPermissions(Integer.valueOf(uid));
        Set<String> permissionNames = new HashSet<>(permissions.size());
        for (Permission permission : permissions) {
            permissionNames.add(permission.getName());
        }
        //此处把当前subject对应的权限信息交给shiro，当调用hasPermission的时候就会根据这些信息判断
        authorizationInfo.setStringPermissions(permissionNames);
        return authorizationInfo;
    }

    /**
     * @MethodName: doGetAuthenticationInfo
     * @Description: TODO 拿用户凭证信息
     * @Param:  * @param authenticationToken 我们可以自己实现的用户凭证/密钥信息
     * @Return: {@link AuthenticationInfo}
     * @Author: yjwu
     * @Date: 2019/12/23 14:01
     **/
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        /**这里为什么是String类型呢？其实要根据Subject.login(token)时候的token来的，你token定义成的pricipal是啥，这里get的时候就是啥。比如我
         Subject subject = SecurityUtils.getSubject();
         UsernamePasswordToken idEmail = new UsernamePasswordToken(String.valueOf(user.getId()), user.getEmail());
         try {
         idEmail.setRememberMe(true);
         subject.login(idEmail);
         }
         **/
        String name = authenticationToken.getPrincipal().toString();
        User user = userService.findUserByUsername(name);
        if (user == null) {
            return null;
        }
        //SimpleAuthenticationInfo还有其他构造方法，比如密码加密算法等，感兴趣可以自己看
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                user,                      //表示凭证，可以随便设，跟token里的一致就行
                user.getPassword(),   //表示密钥如密码，你可以自己随便设，跟token里的一致就行
                getName() //当前realm名称
        );
        //authenticationInfo信息交个shiro，调用login的时候会自动比较这里的token和authenticationInfo
        return authenticationInfo;
    }
}
