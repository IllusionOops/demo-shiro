package com.wyj.controller;

import com.wyj.bean.ResultBean;
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

    @RequestMapping(value = "/test1",method = RequestMethod.GET)
    public ResultBean test1(@RequestParam("param") int param){
        return new ResultBean(200,"success",10/param);
    }

    @GetMapping(value = "/test2")
    public ResultBean test2(@RequestParam("param") int param){
        return new ResultBean(200,"success",10/param);
    }

}
