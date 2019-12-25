package com.wyj.exception;

import com.wyj.bean.ResultBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @ClassName MyExceptionHandler
 * @Description: TODO
 * @Author yjwu
 * @Date 2019/12/24 17:41
 **/
@RestControllerAdvice
public class MyExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResultBean exceptionHandler(Exception e) {
        return new ResultBean(500, "--异常被捕获", e.getMessage());
    }
}
