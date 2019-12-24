package com.wyj.controller;

import com.wyj.bean.ResultBean;
import com.wyj.entity.User;
import com.wyj.service.UserService;
import com.wyj.utils.JWTUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName LoginController
 * @Description: TODO
 * @Author yjwu
 * @Date 2019/12/23 14:08
 **/
@RestController
@Api(value = "SpringBoot集成Shiro JWT测试接口", tags = "LoginController")
public class LoginController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ApiOperation(value = "用户登录接口", notes = "成功会返回JWT token，用于接口调用校验", response = ResultBean.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String")
    })
    public ResultBean login(@RequestParam("username") String username,
                            @RequestParam("password") String password, HttpServletResponse response) {
        ResultBean resultBean = new ResultBean();
        try {
//            Subject subject = SecurityUtils.getSubject();
//            //将用户请求参数封装后，直接提交给Shiro处理
//            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
//            subject.login(token);
//            //Shiro认证通过后会将user信息放到subject内，生成token并返回
//            User user = (User) subject.getPrincipal();
//            String newToken = JWTUtil.createToken(username);
//
//            return new ResultBean(200, "登录成功", JWTUtil.createToken(username));

            User userByUsername = userService.findUserByUsername(username);
            String newPs = new SimpleHash("MD5", password, "abcdefg", 2).toHex();
            if (userByUsername == null) {
                return new ResultBean(401, "用户名错误", null);
            } else if (!newPs.equals(userByUsername.getPassword())) {
                return new ResultBean(401, "密码错误", null);
            } else {
                String token = JWTUtil.createToken(username);
                response.setHeader("x-auth-token", token);
                return new ResultBean(200, "登录成功", token);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultBean(500, "系统错误", null);
        }
    }
    @RequiresRoles(value = {"admin"})
    @PostMapping("/index")
    public Integer index(int numger, HttpServletResponse response) {
        Integer result = 0;
        try {
            result = 9 / numger;
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
            throw new RuntimeException();
        }
        return result;
    }
}
