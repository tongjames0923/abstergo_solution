package com.tbs.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.app.service.impl.token.CookieRefreshTokenService;
import com.tbs.app.web.DefaultUserLoginInfoService;
import com.tbs.broadcast.rabbitmq.support.SimpleRabbitMqSupport;
import com.tbs.broadcast.subscriber.AbstractSubscriber;
import com.tbs.broadcast.subscriber.impl.SimpleUseSubscriber;
import com.tbs.cache.ICacheListenner;
import com.tbs.common.error.IErrorThrowEvent;
import com.tbs.schedule.ISchedule;
import com.tbs.schedule.IScheduleExecuteErrorHandle;
import com.tbs.schedule.IScheduleTask;
import com.tbs.schedule.impl.TimingWheelSchedule;
import com.tbs.shared.interfaces.constraint.PasswordValidator;
import com.tbs.shared.interfaces.constraint.UserNameValidator;
import com.tbs.shared.util.SqlLogicDeleteIntercepter;
import com.tbs.struct.tree.closure.impl.service.ClosureTreeServiceImpl;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.xxl.api.IXXLJobsConfig;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

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

    /**
     * SQL逻辑删除拦截器配置
     * <p>
     * 配置SQL逻辑删除功能，用于处理数据库记录的软删除
     * </p>
     *
     * @return SQL逻辑删除拦截器实例
     */
    @Bean
    public SqlLogicDeleteIntercepter sqlLogicDeleteIntercepter() {
        return new SqlLogicDeleteIntercepter();
    }

    /**
     * 错误抛出事件配置
     * <p>
     * 配置全局错误处理事件，用于记录和处理系统中的错误
     * </p>
     *
     * @return 错误抛出事件实例
     */
    @Bean
    IErrorThrowEvent errorThrowEvent() {
        return new IErrorThrowEvent() {
            @Override
            public boolean onAssertUtilThrow(Throwable throwable) {
                logger.error("AssertUtil error", throwable);
                return false;
            }

            private Logger logger = LoggerFactory.getLogger(DefaultAppConfig.class);
        };
    }

    /**
     * Redisson编解码器配置
     * <p>
     * 配置Redis使用的JSON编解码器，用于对象的序列化和反序列化
     * </p>
     *
     * @param objectMapper Jackson对象映射器
     * @return Redisson编解码器实例
     */
    @Bean
    public Codec codec(ObjectMapper objectMapper) {
        return new JsonJacksonCodec(objectMapper);
    }

    /**
     * 缓存监听器配置
     * <p>
     * 配置缓存监听器，用于记录缓存的各种操作（获取、放入、移除、清除）
     * </p>
     *
     * @return 缓存监听器实例
     */
    @Bean
    ICacheListenner cacheListenner() {
        return new ICacheListenner() {

            private Logger logger = LoggerFactory.getLogger(DefaultAppConfig.class);

            @Override
            public boolean support(Cache cache) {
                return true;
            }

            @Override
            public void onGet(String cacheName, Object key, Object value) {
                logger.info("cache {} hit at {} with {}", cacheName, key, value);
            }

            @Override
            public void onPut(String cacheName, Object key, Object value) {
                logger.info("cache {} put {} with {}", cacheName, key, value);
            }

            @Override
            public void onEvict(String cacheName, Object key) {
                logger.info("cache {} evict {}", cacheName, key);
            }

            @Override
            public void onClear(String cacheName) {
                logger.info("cache {} clear", cacheName);
            }
        };
    }

    private static final Logger sub_test_logger = LoggerFactory.getLogger("sub_test");

    /**
     * 事件订阅者配置
     * <p>
     * 配置简单的事件订阅者，用于处理系统中的事件
     * </p>
     *
     * @return 事件订阅者实例
     */
    @Bean
    AbstractSubscriber subscriber() {
        return SimpleUseSubscriber.Builder.newBuilder("simple log", (e, p) -> {
            sub_test_logger.info("event id {},event name :{}", e.getEventId(), e.getEventName());
            return AbstractSubscriber.RECEIVE_SUCCESS;
        }).build();
    }

    private static final Logger system_oper_log = LoggerFactory.getLogger("system_oper_log");

    //    @Bean
    //    AbstractSubscriber systemLogSub() {
    //        return SimpleUseSubscriber.Builder.newBuilder("system_oper_log", (event, publisher) -> {
    //            if (event == null) {
    //                system_oper_log.warn("event null for receive");
    //                return AbstractSubscriber.RECEIVE_FAILED;
    //            }
    //            var systemOperateRecordEntity = event.getEventDataClass().map(clazz -> {
    //                if (clazz.equals(SystemOperateRecordEntity.class) && event instanceof SimpleEvent) {
    //                    SimpleEvent event1 = (SimpleEvent)event;
    //                    return event1.getDeserializedData();
    //                }
    //                return null;
    //            });
    //            systemOperateRecordEntity.ifPresent(record -> {
    //                system_oper_log.info("system operate log {}", record);
    //            });
    //            return AbstractSubscriber.RECEIVE_SUCCESS;
    //        }).supportEventIds(Arrays.asList(EventSystemOpLog.DEFAULT_EVENT_ID)).build();
    //    }

    //    @Bean
    //    ISystemOperateLog systemOperateLog() {
    //        var i = new DbSystemOpLog();
    //        i.setBatchSize(100);
    //        return i;
    //    }

    /**
     * RabbitMQ支持配置
     * <p>
     * 配置RabbitMQ支持组件，用于简化RabbitMQ的使用
     * </p>
     *
     * @return RabbitMQ支持实例
     */
    @Bean
    SimpleRabbitMqSupport rabbitMqSupport() {
        return new SimpleRabbitMqSupport();
    }

    private static final Logger timingWheelScheduleLogger = LoggerFactory.getLogger(TimingWheelSchedule.class);

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
     * 时间轮调度器配置
     * <p>
     * 配置时间轮调度器，用于处理定时任务
     * </p>
     *
     * @return 时间轮调度器实例
     */
    @Bean
    TimingWheelSchedule timingWheelSchedule() {
        return new TimingWheelSchedule(new IScheduleExecuteErrorHandle() {
            @Override
            public void handle(IScheduleTask task, Throwable throwable, ISchedule schedule) {
                timingWheelScheduleLogger.error("处理任务时产生错误", throwable);
            }
        }, 5L, TimeUnit.SECONDS, 1L, 60 * 5, TimeUnit.SECONDS);
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
    ClosureTreeServiceImpl treeStore() {
        return new ClosureTreeServiceImpl();
    }
}
