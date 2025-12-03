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
 * @author tongj
 */
@Configuration
public class AppConfig {

    @Bean
    public SqlLogicDeleteIntercepter sqlLogicDeleteIntercepter() {
        return new SqlLogicDeleteIntercepter();
    }

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

    @Bean
    public Codec codec(ObjectMapper objectMapper) {
        return new JsonJacksonCodec(objectMapper);
    }

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

    @Bean
    SimpleRabbitMqSupport rabbitMqSupport() {
        return new SimpleRabbitMqSupport();
    }

    private static final Logger timingWheelScheduleLogger = LoggerFactory.getLogger(TimingWheelSchedule.class);

    @Bean
    public IUserLoginInfoService userLoginInfoService() {
        return new DefaultUserLoginInfoService();
    }

    @Bean
    IRefreshTokenService refreshTokenService() {
        return new CookieRefreshTokenService();
    }

    @Bean
    TimingWheelSchedule timingWheelSchedule() {
        return new TimingWheelSchedule(new IScheduleExecuteErrorHandle() {
            @Override
            public void handle(IScheduleTask task, Throwable throwable, ISchedule schedule) {
                timingWheelScheduleLogger.error("处理任务时产生错误", throwable);
            }
        }, 5L, TimeUnit.SECONDS, 1L, 60 * 5, TimeUnit.SECONDS);
    }

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

    @Bean
    ClosureTreeServiceImpl treeStore() {
        return new ClosureTreeServiceImpl();
    }
}
