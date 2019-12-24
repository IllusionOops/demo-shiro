package com.wyj.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName ResultBean
 * @Description: TODO
 * @Author yjwu
 * @Date 2019/12/24 13:14
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultBean {
    private int code;
    private String msg;
    private Object obj;
}
