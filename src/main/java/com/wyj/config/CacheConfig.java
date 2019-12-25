package com.wyj.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.lang.reflect.Method;

/**
 * @ClassName CacheConfig
 * @Description: TODO
 * @Author yjwu
 * @Date 2019/12/25 13:39
 **/
@Slf4j
@Configuration
@ConditionalOnBean(RedisConfig.class)
public class CacheConfig extends CachingConfigurerSupport {
    @Autowired
    @Qualifier(value = "redisCacheManager")
    private RedisCacheManager redisCacheManager;

    public CacheConfig() {
        super();
    }
    /**
     * @MethodName: cacheManager
     * @Description: TODO 设置默认的缓存管理器
     * @Param:  * @param
     * @Return: {@link CacheManager}
     * @Author: yjwu
     * @Date: 2019/12/25 13:43
     **/
    @Override
    public CacheManager cacheManager() {
        return redisCacheManager;
    }

    @Override
    public CacheResolver cacheResolver() {
        return super.cacheResolver();
    }

    //key生成器配置，进行方法级别的缓存
    @Override
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... objects) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(target.getClass().getName());
                stringBuffer.append(":");
                stringBuffer.append(method.getName());
                for (Object object : objects) {
                    stringBuffer.append(String.valueOf(object));
                }
                return stringBuffer.toString();
            }
        };
    }

    @Override
    public CacheErrorHandler errorHandler() {
        // 异常处理，当Redis发生异常时，打印日志，但是程序正常走
        log.info("初始化 -> [{}]", "Redis CacheErrorHandler");
        CacheErrorHandler cacheErrorHandler = new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.error("Redis occur handleCacheGetError：key -> [{}]", key, e);
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.error("Redis occur handleCachePutError：key -> [{}]；value -> [{}]", key, value, e);
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.error("Redis occur handleCacheEvictError：key -> [{}]", key, e);
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.error("Redis occur handleCacheClearError：", e);
            }
        };
        return cacheErrorHandler;
    }
}
