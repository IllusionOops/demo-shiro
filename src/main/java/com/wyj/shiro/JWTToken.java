package com.wyj.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * @ClassName JWTToken
 * @Description: TODO 重新实现AuthenticationToken类，让其存放token，便于校验。
 * @Author yjwu
 * @Date 2019/12/24 10:49
 **/
public class JWTToken implements AuthenticationToken {
    private String token;

    /**
     * Instantiates a new Jwt token.
     *
     * @param token the token
     */
    public JWTToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
