package com.wyj.config;

import com.wyj.entity.User;
import com.wyj.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

/**
 * @ClassName DbShiroRealm
 * @Description: TODO
 * @Author yjwu
 * @Date 2019/12/24 10:39
 **/
public class DbShiroRealm extends AuthorizingRealm {
    //数据库存储的用户密码的加密salt，正式环境不能放在源代码里
    private static final String encryptSalt = "F12839WhsnnEV$#23b";
    private UserService userService;

    public DbShiroRealm(UserService userService) {
        this.userService = userService;
        //因为数据库中的密码做了散列，所以使用shiro的散列Matcher
        this.setCredentialsMatcher(new HashedCredentialsMatcher(Sha256Hash.ALGORITHM_NAME));
    }

    /**
     * 找它的原因是这个方法返回true
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }

    /**
     * 这一步我们根据token给的用户名，去数据库查出加密过用户密码，然后把加密后的密码和盐值一起发给shiro，让它做比对
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken userpasswordToken = (UsernamePasswordToken) token;
        String username = userpasswordToken.getUsername();
        User userByUsername = userService.findUserByUsername(username);
        if (userByUsername == null){
            throw new AuthenticationException("用户名或者密码错误");
        }
        return new SimpleAuthenticationInfo(userByUsername, userByUsername.getPassword(), ByteSource.Util.bytes(encryptSalt), "dbRealm");
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }
}
