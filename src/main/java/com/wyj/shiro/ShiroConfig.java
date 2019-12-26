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
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.serializer.ObjectSerializer;
import org.crazycake.shiro.serializer.StringSerializer;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.Filter;
import java.util.*;


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
     * @Param: * @param securityManager
     * @Return: {@link ShiroFilterFactoryBean}
     * @Author: yjwu
     * @Date: 2019/12/25 13:52
     **/
    @Bean
    public ShiroFilterFactoryBean factory(SecurityManager securityManager) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager);
        //设置后台登录接口，而不是页面。如果不设置，默认会寻找web目录下的login.jsp
//        factoryBean.setLoginUrl("/login");
        //设置登录成功后的跳转接口而非页面
//        factoryBean.setSuccessUrl("/index");
        // 设置无权限时跳转的接口;
        factoryBean.setUnauthorizedUrl("/unauthorized/无权限");

        //设置自定义拦截器
        Map<String, Filter> filterMap = new HashMap<>();
        //设置我们自定义的JWT过滤器
        filterMap.put("jwt", new JWTFilter());
        factoryBean.setFilters(filterMap);


        //过滤链配置，从上向下执行，顺序很重要，所以使用linkedhashmap较好
        Map<String, String> filterRuleMap = new LinkedHashMap<>();
        //访问无需登录即可访问的资源。anno表示可以匿名访问，不需要经过过滤器
        filterRuleMap.put("/login", "anon");
        //logout是shiro提供的过滤器
        filterRuleMap.put("/logout", "logout");
        filterRuleMap.put("/unauthorized/**", "anon");
        filterRuleMap.put("/swagger-**/**", "anon");
        //此时访问/userInfo/del需要del权限,在自定义Realm中为用户授权。
        //filterChainDefinitionMap.put("/userInfo/del", "perms[\"userInfo:del\"]");
        //所有请求通过我们自己的JWT Filter
        filterRuleMap.put("/**", "jwt");
        factoryBean.setFilterChainDefinitionMap(filterRuleMap);
        return factoryBean;
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
//        securityManager.setRememberMeManager(rememberMeManager());
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
        log.info("shiroSessionListener.getSessionCount()=" + shiroSessionListener.getSessionCount());
        return shiroSessionListener;
    }

    //
    @Bean(name = "shiroRedisManager")
    public RedisManager redisManager() {
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(host + ":" + port);
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
        //cookie名字
        SimpleCookie simpleCookie = new SimpleCookie("shiroSessionId");
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

    /**
     * cookie对象;会话Cookie模板 ,默认为: JSESSIONID 问题: 与SERVLET容器名冲突,重新定义为sid或rememberMe，自定义
     *
     * @return
     */
    @Bean
    public SimpleCookie rememberMeCookie() {
        //这个参数是cookie的名称，对应前端的checkbox的name = rememberMe
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
        //setcookie的httponly属性如果设为true的话，会增加对xss防护的安全系数。它有以下特点：
        //setcookie()的第七个参数
        //设为true后，只能通过http访问，javascript无法访问
        //防止xss读取cookie
        simpleCookie.setHttpOnly(true);
        simpleCookie.setPath("/");
        //<!-- 记住我cookie生效时间30天 ,单位秒;-->
        simpleCookie.setMaxAge(2592000);
        return simpleCookie;
    }

    /**
     * cookie管理对象;记住我功能,rememberMe管理器
     *
     * @return
     */
    @Bean
    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie());
        //rememberMe cookie加密的密钥 建议每个项目都不一样 默认AES算法 密钥长度(128 256 512 位)
        Base64.Decoder decoder = Base64.getDecoder();
        cookieRememberMeManager.setCipherKey(decoder.decode("4AvVhmFLUs0KTA3Kprsdag=="));
        return cookieRememberMeManager;
    }

    /**
     * FormAuthenticationFilter 过滤器 过滤记住我
     *
     * @return
     */
    @Bean
    public FormAuthenticationFilter formAuthenticationFilter() {
        FormAuthenticationFilter formAuthenticationFilter = new FormAuthenticationFilter();
        //对应前端的checkbox的name = rememberMe
        formAuthenticationFilter.setRememberMeParam("rememberMe");
        return formAuthenticationFilter;
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
        sessionManager.setCacheManager(redisCacheManager());

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
        sessionManager.setSessionIdUrlRewritingEnabled(false);

        return sessionManager;
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


    /**
     * 解决： 无权限页面不跳转 shiroFilterFactoryBean.setUnauthorizedUrl("/unauthorized") 无效
     * shiro的源代码ShiroFilterFactoryBean.Java定义的filter必须满足filter instanceof AuthorizationFilter，
     * 只有perms，roles，ssl，rest，port才是属于AuthorizationFilter，而anon，authcBasic，auchc，user是AuthenticationFilter，
     * 所以unauthorizedUrl设置后页面不跳转 Shiro注解模式下，登录失败与没有权限都是通过抛出异常。
     * 并且默认并没有去处理或者捕获这些异常。在SpringMVC下需要配置捕获相应异常来通知用户信息
     * 在resources/templates目录下添加界面
     *
     * @return
     */
    @Bean
    public SimpleMappingExceptionResolver simpleMappingExceptionResolver() {
        SimpleMappingExceptionResolver simpleMappingExceptionResolver = new SimpleMappingExceptionResolver();
        Properties properties = new Properties();
        //这里的 /unauthorized 是页面，不是访问的路径
        properties.setProperty("org.apache.shiro.authz.UnauthorizedException", "/unauthorized");
        properties.setProperty("org.apache.shiro.authz.UnauthenticatedException", "/unauthorized");
        simpleMappingExceptionResolver.setExceptionMappings(properties);
        return simpleMappingExceptionResolver;
    }

    /**
     * 解决spring-boot2.0 Whitelabel Error Page跳转
     *
     * @return
     */
    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/unauthorized.html");
            ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404.html");
            ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500.html");
            factory.addErrorPages(error401Page, error404Page, error500Page);
        };
    }
}
