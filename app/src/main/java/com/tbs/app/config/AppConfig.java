package com.tbs.app.config;

import com.tbs.app.service.impl.token.CookieRefreshTokenService;
import com.tbs.app.web.DefaultUserLoginInfoService;
import com.tbs.broadcast.rabbitmq.support.SimpleRabbitMqSupport;
import com.tbs.cache.ICacheListenner;
import com.tbs.common.i18n.AbstractMessageSourceProvider;
import com.tbs.logging.core.ILogRecorder;
import com.tbs.logging.rabbitmq.RabbitMqLoggingRecorder;
import com.tbs.mq.RedisFaildMessageStore;
import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.schedule.RedisTaskStore;
import com.tbs.schedule.api.ScheduleTrigger;
import com.tbs.schedule.api.TaskStore;
import com.tbs.schedule.xxl.XxlJobScheduleTrigger;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.user.IUserLoginInfoService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.Kryo5Codec;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * 应用程序配置类
 * <p>
 * 该类负责配置应用程序的各种组件和服务，包括缓存、消息队列、定时任务等
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Configuration
@Slf4j
public class AppConfig {

    @Bean
    public AbstractMessageSourceProvider appMessageSourceProvider() {
        return new AbstractMessageSourceProvider() {
            @Override
            public String getBasename() {
                return "i18n/messages-app";
            }
        };
    }

    @Bean
    SimpleRabbitMqSupport rabbitMqSupport() {
        return new SimpleRabbitMqSupport();
    }

    @Bean
    public IFailedMessageStore systemFailedMessageStore(RedissonClient client) {
        return new RedisFaildMessageStore(client);
    }

    @Bean
    public ILogRecorder loggerRecoreder() {
        return new RabbitMqLoggingRecorder("tbs.log.queue", "systemLog");
    }

    /**
     * 用户登录信息服务配置
     * <p>
     * 配置用户登录信息服务，用于处理用户登录相关的信息
     * </p>
     *
     * @return 用户登录信息服务实例
     */
    @Bean
    public IUserLoginInfoService userLoginInfoService() {
        return new DefaultUserLoginInfoService();
    }

    /**
     * 刷新令牌服务配置
     * <p>
     * 配置刷新令牌服务，用于处理用户令牌的刷新
     * </p>
     *
     * @return 刷新令牌服务实例
     */
    @Bean
    IRefreshTokenService refreshTokenService() {
        return new CookieRefreshTokenService();
    }

//    @Bean
//    public ICacheListenner logCacheListenner() {
//        return new ICacheListenner() {
//            /**
//             * 判断是否支持指定的缓存
//             * <p>
//             * 用于确定当前监听器是否应该处理指定的缓存实例
//             * </p>
//             *
//             * @param cache 要检查的缓存实例
//             * @return 如果支持则返回true，否则返回false
//             */
//            @Override
//            public boolean support(Cache cache) {
//                return true;
//            }
//
//            /**
//             * 缓存获取事件回调
//             * <p>
//             * 当从缓存中获取值时触发此方法
//             * </p>
//             *
//             * @param cacheName 缓存名称
//             * @param key       缓存键
//             * @param value     缓存值
//             */
//            @Override
//            public void onGet(String cacheName, Object key, Object value) {
//                log.info("Cache Get: cacheName={}, key={}, value={}", cacheName, key, value);
//            }
//
//            /**
//             * 缓存设置事件回调
//             * <p>
//             * 当向缓存中设置值时触发此方法
//             * </p>
//             *
//             * @param cacheName 缓存名称
//             * @param key       缓存键
//             * @param value     缓存值
//             */
//            @Override
//            public void onPut(String cacheName, Object key, Object value) {
//                log.info("Cache Put: cacheName={}, key={}, value={}", cacheName, key, value);
//
//            }
//
//            /**
//             * 缓存删除事件回调
//             * <p>
//             * 当从缓存中删除值时触发此方法
//             * </p>
//             *
//             * @param cacheName 缓存名称
//             * @param key       缓存键
//             */
//            @Override
//            public void onEvict(String cacheName, Object key) {
//                log.info("Cache Evict: cacheName={}, key={}", cacheName, key);
//
//            }
//
//            /**
//             * 缓存清除事件回调
//             * <p>
//             * 当清除缓存中的所有值时触发此方法
//             * </p>
//             *
//             * @param cacheName 缓存名称
//             */
//            @Override
//            public void onClear(String cacheName) {
//                log.info("Cache Clear: cacheName={}", cacheName);
//
//            }
//
//            /**
//             * 缓存过期事件回调
//             * <p>
//             * 当缓存项过期时触发此方法
//             * </p>
//             *
//             * @param cacheName  缓存名称
//             * @param key        缓存键
//             * @param expireTime 过期时间
//             */
//            @Override
//            public void calledExpire(String cacheName, Object key, Date expireTime) {
//                log.info("Cache Expire: cacheName={}, key={}, expireTime={}", cacheName, key, expireTime);
//
//            }
//        };
//    }

    @Bean
    public ScheduleTrigger xxlJobTrigger() {
        return new XxlJobScheduleTrigger();
    }

    @Bean
    TaskStore redisTaskStore() {
        return new RedisTaskStore();
    }

    @Bean
    Codec redissonCodec() {
        return new Kryo5Codec();
    }
}

