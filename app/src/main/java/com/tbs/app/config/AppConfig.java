package com.tbs.app.config;

import com.tbs.app.service.impl.token.CookieRefreshTokenService;
import com.tbs.app.web.DefaultUserLoginInfoService;
import com.tbs.broadcast.rabbitmq.support.SimpleRabbitMqSupport;
import com.tbs.common.i18n.AbstractMessageSourceProvider;
import com.tbs.logging.core.ILogRecorder;
import com.tbs.logging.rabbitmq.RabbitMqLoggingRecorder;
import com.tbs.mq.RedisFaildMessageStore;
import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.user.IUserLoginInfoService;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
public class AppConfig {

    @Bean
    public AbstractMessageSourceProvider appMessageSourceProvider() {
        return new AbstractMessageSourceProvider() {
            @Override
            public  String getBasename() {
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

}
