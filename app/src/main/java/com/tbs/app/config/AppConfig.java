package com.tbs.app.config;

import com.tbs.app.service.impl.token.CookieRefreshTokenService;
import com.tbs.app.web.DefaultUserLoginInfoService;
import com.tbs.broadcast.rabbitmq.support.SimpleRabbitMqSupport;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import com.tbs.struct.tree.closure.impl.service.ClosureTreeServiceImpl;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.xxl.api.IXXLJobsConfig;
import org.apache.dubbo.config.annotation.DubboService;
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
    SimpleRabbitMqSupport rabbitMqSupport() {
        return new SimpleRabbitMqSupport();
    }

    //    /**
    //     * 缓存监听器配置
    //     * <p>
    //     * 配置缓存监听器，用于记录缓存的各种操作（获取、放入、移除、清除）
    //     * </p>
    //     *
    //     * @return 缓存监听器实例
    //     */
    //    @Bean
    //    ICacheListenner cacheListenner() {
    //        return new ICacheListenner() {
    //
    //            private Logger logger = LoggerFactory.getLogger(DefaultAppConfig.class);
    //
    //            @Override
    //            public boolean support(Cache cache) {
    //                return true;
    //            }
    //
    //            @Override
    //            public void onGet(String cacheName, Object key, Object value) {
    //                logger.info("cache {} hit at {} with {}", cacheName, key, value);
    //            }
    //
    //            @Override
    //            public void onPut(String cacheName, Object key, Object value) {
    //                logger.info("cache {} put {} with {}", cacheName, key, value);
    //            }
    //
    //            @Override
    //            public void onEvict(String cacheName, Object key) {
    //                logger.info("cache {} evict {}", cacheName, key);
    //            }
    //
    //            @Override
    //            public void onClear(String cacheName) {
    //                logger.info("cache {} clear", cacheName);
    //            }
    //        };
    //    }
    //
    //    private static final Logger sub_test_logger = LoggerFactory.getLogger("sub_test");

    /**
     * 事件订阅者配置
     * <p>
     * 配置简单的事件订阅者，用于处理系统中的事件
     * </p>
     *
     * @return 事件订阅者实例
     */
    //    @Bean
    //    AbstractSubscriber subscriber() {
    //        return SimpleUseSubscriber.Builder.newBuilder("simple log", (e, p) -> {
    //            sub_test_logger.info("event id {},event name :{}", e.getEventId(), e.getEventName());
    //            return AbstractSubscriber.RECEIVE_SUCCESS;
    //        }).build();
    //    }

    //    private static final Logger timingWheelScheduleLogger = LoggerFactory.getLogger(TimingWheelSchedule.class);

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


    /**
     * XXL-Job配置
     * <p>
     * 配置XXL-Job分布式任务调度中心的连接信息
     * </p>
     *
     * @return XXL-Job配置实例
     */
    @Bean
    IXXLJobsConfig xxlJobsConfig() {
        return new IXXLJobsConfig() {
            @Override
            public String adminAddress() {
                return "http://127.0.0.1:9898/xxl-job-admin";
            }

            @Override
            public String appName() {
                return "mainSystemApplication";
            }

            @Override
            public String address() {
                return "";
            }

            @Override
            public String ip() {
                return "";
            }

            @Override
            public Integer port() {
                return null;
            }

            @Override
            public String accessToken() {
                return "123456789";
            }

            @Override
            public String logPath() {
                return "";
            }

            @Override
            public Integer logRetentionsDays() {
                return 0;
            }
        };
    }

    /**
     * 闭包树服务配置
     * <p>
     * 配置闭包树服务，用于处理树形结构数据
     * </p>
     *
     * @return 闭包树服务实例
     */
    @Bean
    @DubboService
    ITreeStore treeStore() {
        return new ClosureTreeServiceImpl();
    }
}
