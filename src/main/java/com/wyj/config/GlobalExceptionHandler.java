package com.wyj.config;

import com.wyj.bean.ResultBean;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName GlobalExceptionHandler
 * @Description: TODO
 * @Author yjwu
 * @Date 2019/12/23 14:08
 **/
@ControllerAdvice
public class GlobalExceptionHandler {


    /**
     * Handle 401 api result.
     * 捕捉shiro的异常
     *
     * @return the api result
     */
    @ExceptionHandler(ShiroException.class)
    public ResultBean handle401() {
        return new ResultBean(401, "您没有权限访问！", null);
    }

    /**
     * Global exception api result.
     * 捕捉其他所有异常
     *
     * @param request the request
     * @param ex      the ex
     * @return the api result
     */
    @ExceptionHandler(Exception.class)
    public ResultBean globalException(HttpServletRequest request, Throwable ex) {
        ex.printStackTrace();
        return new ResultBean(401, "访问出错，无法访问: " + ex.getMessage(), null);
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.valueOf(statusCode);
    }
}