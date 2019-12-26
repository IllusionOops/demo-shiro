package com.wyj.controller;

import com.wyj.bean.ResultBean;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName IndexController
 * @Description: TODO
 * @Author yjwu
 * @Date 2019/12/24 17:34
 **/
@RestController
@RequestMapping("/index")
public class IndexController {

    @RequiresRoles(value = "admin")
    @RequestMapping(value = "/test1",method = RequestMethod.GET)
    public ResultBean test1(@RequestParam("param") int param)
    {
        Subject subject = SecurityUtils.getSubject();
        return new ResultBean(200,"success",10/param);
    }

    @GetMapping(value = "/test2")
    public ResultBean test2(@RequestParam("param") int param){
        String newPs = new SimpleHash("MD5", "123", "abcdefg", 2).toHex();
        System.out.println("newPs====="+newPs);
        return new ResultBean(200,newPs,10/param);
    }

}
