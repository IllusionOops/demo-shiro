package com.wyj.shiro;

import com.wyj.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.serializer.ObjectSerializer;
import org.crazycake.shiro.serializer.StringSerializer;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * @ClassName ShiroConfig
 * @Description: TODO
 * @Author yjwu
 * @Date 2019/12/23 14:05
 **/
@Configuration
@Slf4j
public class ShiroConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.jedis.pool.port}")
    private String port;
    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public UserRealm userRealm() {
        return new UserRealm();
    }

    /**
     * @MethodName: factory
     * @Description: TODO 先走 filter ，然后 filter 如果检测到请求头存在 token，则用 token 去 login，走 Realm 去验证
     * @Param:  * @param securityManager
     * @Return: {@link ShiroFilterFactoryBean}
     * @Author: yjwu
     * @Date: 2019/12/25 13:52
     **/
    @Bean
    public ShiroFilterFactoryBean factory(SecurityManager securityManager) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        Map<String, Filter> filterMap = new HashMap<>();
        //设置我们自定义的JWT过滤器
        filterMap.put("jwt", new JWTFilter());
        factoryBean.setFilters(filterMap);
        factoryBean.setSecurityManager(securityManager);
        // 设置无权限时跳转的 url;
        factoryBean.setUnauthorizedUrl("/unauthorized/无权限");
        Map<String, String> filterRuleMap = new HashMap<>();
        //访问/login和/unauthorized 不需要经过过滤器
        filterRuleMap.put("/login", "anon");
//        filterRuleMap.put("/logout", "logout");
//        filterRuleMap.put("/index/**", "anon");
        filterRuleMap.put("/unauthorized/**", "anon");
//        filterRuleMap.put("/swagger-**/**", "anon");
        // 所有请求通过我们自己的JWT Filter
//        filterRuleMap.put("/**", "jwt");
        filterRuleMap.put("/**", "authc");
        // 访问 /unauthorized/** 不通过JWTFilter
        factoryBean.setFilterChainDefinitionMap(filterRuleMap);
        return factoryBean;
    }

    /**
     * @MethodName: sessionIdGenerator
     * @Description: TODO 配置会话id生成器
     * @Param: * @param
     * @Return: {@link SessionIdGenerator}
     * @Author: yjwu
     * @Date: 2019/12/25 11:15
     **/
    @Bean
    public SessionIdGenerator sessionIdGenerator() {
        return new JavaUuidSessionIdGenerator();
    }

    /**
     * @MethodName: shiroSessionListener
     * @Description: TODO 配置session监听
     * @Param: * @param
     * @Return: {@link ShiroSessionListener}
     * @Author: yjwu
     * @Date: 2019/12/25 11:14
     **/
    @Bean("shiroSessionListener")
    public ShiroSessionListener shiroSessionListener() {
        ShiroSessionListener shiroSessionListener = new ShiroSessionListener();
        log.info("shiroSessionListener.getSessionCount()="+shiroSessionListener.getSessionCount());
        return shiroSessionListener;
    }

    //
    @Bean(name = "shiroRedisManager")
    public RedisManager redisManager() {
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(host+":"+port);
        redisManager.setPassword(password);
        // 配置过期时间
        redisManager.setTimeout(1800);
        return redisManager;
    }

    @Bean("shiroRedisCacheManager")
    public RedisCacheManager redisCacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager());
        redisCacheManager.setKeySerializer(new StringSerializer());
        redisCacheManager.setValueSerializer(new ObjectSerializer());
        return redisCacheManager;
    }

    @Bean
    public SessionDAO sessionDAO() {
        EnterpriseCacheSessionDAO enterpriseCacheSessionDAO = new EnterpriseCacheSessionDAO();
        //使用缓存管理器
        enterpriseCacheSessionDAO.setCacheManager(redisCacheManager());
        //设置session缓存的名字。默认为shiro-activeSessionCache
        enterpriseCacheSessionDAO.setActiveSessionsCacheName("shiro-activeSessionCache");
        //sessionid生成器
        enterpriseCacheSessionDAO.setSessionIdGenerator(sessionIdGenerator());
        return enterpriseCacheSessionDAO;
    }

    /**
     * @MethodName: sessionIdCookie
     * @Description: TODO 配置sessionid的cookie。这里的cookie不是 记住我 中的cookie，是两个不同的cookie
     * @Param: * @param
     * @Return: {@link SimpleCookie}
     * @Author: yjwu
     * @Date: 2019/12/25 11:30
     **/
    @Bean("sessionIdCookie")
    public SimpleCookie sessionIdCookie() {
        //cooki名字
        SimpleCookie simpleCookie = new SimpleCookie("shirosessionid");
        //setcookie的httponly属性如果设为true的话，会增加对xss防护的安全系数。它有以下特点：
        //setcookie()的第七个参数
        //设为true后，只能通过http访问，javascript无法访问
        //防止xss读取cookie
        simpleCookie.setHttpOnly(true);
        simpleCookie.setPath("/");
        //浏览器关闭时失效此cookie
        simpleCookie.setMaxAge(-1);
        return simpleCookie;
    }

    @Bean
    public SessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        Collection<SessionListener> listeners = new ArrayList<SessionListener>();
        //配置监听
        listeners.add(shiroSessionListener());
        sessionManager.setSessionListeners(listeners);
        sessionManager.setSessionIdCookie(sessionIdCookie());
        sessionManager.setSessionDAO(sessionDAO());
//        sessionManager.setCacheManager(ehCacheManager());

        //全局会话超时时间（单位毫秒），默认30分钟  暂时设置为10秒钟 用来测试
        sessionManager.setGlobalSessionTimeout(1800000);
        //是否开启删除无效的session对象  默认为true
        sessionManager.setDeleteInvalidSessions(true);
        //是否开启定时调度器进行检测过期session 默认为true
        sessionManager.setSessionValidationSchedulerEnabled(true);
        //设置session失效的扫描时间, 清理用户直接关闭浏览器造成的孤立会话 默认为 1个小时
        //设置该属性 就不需要设置 ExecutorServiceSessionValidationScheduler 底层也是默认自动调用ExecutorServiceSessionValidationScheduler
        //暂时设置为 5秒 用来测试
        sessionManager.setSessionValidationInterval(3600000);

        //取消url 后面的 JSESSIONID
//        sessionManager.setSessionIdUrlRewritingEnabled(false);

        return sessionManager;
    }

    /**
     * @MethodName: securityManager
     * @Description: TODO 配置shiro核心安全事务管理器。
     * @Param: * @param
     * @Return: {@link SecurityManager}
     * @Author: yjwu
     * @Date: 2019/12/25 11:34
     **/
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 设置自定义 realm.
        securityManager.setRealm(userRealm());
        //        securityManager.setRememberMeManager();
        //关闭shiro自带的session，详情见文档 http://shiro.apache.org/session-management.html#SessionManagement-StatelessApplications%28Sessionless%29
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(true);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        // 自定义缓存实现 使用redis
        securityManager.setCacheManager(redisCacheManager());
        // 自定义缓存实现 使用redis
        securityManager.setSessionManager(sessionManager());

        securityManager.setSubjectDAO(subjectDAO);
        return securityManager;
    }



    /**
     * 开启Shiro的注解(如@RequiresRoles,@RequiresPermissions),需借助SpringAOP扫描使用Shiro注解的类,并在必要时进行安全逻辑验证
     * 配置以下两个bean(DefaultAdvisorAutoProxyCreator和AuthorizationAttributeSourceAdvisor)即可实现此功能
     *
     * @return
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    /**
     * 开启aop注解支持
     *
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }
}
