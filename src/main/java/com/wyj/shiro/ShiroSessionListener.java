package com.wyj.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName ShiroSessionListener
 * @Description: TODO shiro配置会话管理。session监听类，实现sessionlistener接口
 * @Author yjwu
 * @Date 2019/12/25 11:08
 **/
public class ShiroSessionListener implements SessionListener {
    //统计在线人数
    private final AtomicInteger sessionCount = new AtomicInteger(0);

    /**
     * @MethodName: onStart
     * @Description: TODO 会话创建时触发
     * @Param: * @param session
     * @Return:
     * @Author: yjwu
     * @Date: 2019/12/25 11:10
     **/
    @Override
    public void onStart(Session session) {
        sessionCount.incrementAndGet();
    }

    /**
     * @MethodName: onStop
     * @Description: TODO 会话退出时触发
     * @Param: * @param session
     * @Return:
     * @Author: yjwu
     * @Date: 2019/12/25 11:10
     **/
    @Override
    public void onStop(Session session) {
        sessionCount.decrementAndGet();
    }

    /**
     * @MethodName: onExpiration
     * @Description: TODO 会话过期时触发
     * @Param:  * @param session
     * @Return:
     * @Author: yjwu
     * @Date: 2019/12/25 11:11
     **/
    @Override
    public void onExpiration(Session session) {
        sessionCount.decrementAndGet();
    }

    /**
     * @MethodName: getSessionCount
     * @Description: TODO 获取在线人数
     * @Param:  * @param
     * @Return: {@link AtomicInteger}
     * @Author: yjwu
     * @Date: 2019/12/25 11:12
     **/
    public AtomicInteger getSessionCount(){
        return sessionCount;
    }
}
