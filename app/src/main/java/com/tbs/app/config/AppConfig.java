package com.tbs.app.config;

import com.tbs.app.pojo.entity.SystemOperateRecordEntity;
import com.tbs.app.service.log.ISystemOperateLog;
import com.tbs.app.service.log.impl.EventSystemOpLog;
import com.tbs.broadcast.event.impl.SimpleEvent;
import com.tbs.broadcast.subscriber.AbstractSubscriber;
import com.tbs.broadcast.subscriber.impl.SimpleUseSubscriber;
import com.tbs.cache.core.ICacheListenner;
import com.tbs.cache.core.manager.holder.DataHolderCacheManager;
import com.tbs.common.error.IErrorThrowEvent;
import com.tbs.schedule.ISchedule;
import com.tbs.schedule.IScheduleExecuteErrorHandle;
import com.tbs.schedule.IScheduleTask;
import com.tbs.schedule.impl.TimingWheelSchedule;
import com.tbs.sql.logger.ISqlLogger;
import com.tbs.web.trace.core.AbstractTraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tbs.framework.sql.interfaces.impls.SimpleSqlLogger;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author tongj
 */
@Configuration
public class AppConfig {
    @Bean
    IErrorThrowEvent errorThrowEvent() {
        return new IErrorThrowEvent() {
            @Override
            public boolean onAssertUtilThrow(Throwable throwable) {
                logger.debug("will throw error {}", throwable.getMessage());
                return false;
            }

            private Logger logger = LoggerFactory.getLogger(AppConfig.class);
        };
    }

    @Bean
    ICacheListenner cacheListenner() {
        return new ICacheListenner() {

            @Resource
            AbstractTraceContext traceContext;
            private Logger logger = LoggerFactory.getLogger(AppConfig.class);

            @Override
            public boolean support(Cache cache) {
                return true;
            }

            @Override
            public void onGet(String cacheName, Object key, Object value) {
                logger.info("trace:{} ,get cache {} {} {}", traceContext.getRootTrace(), cacheName, key, value);
            }

            @Override
            public void onPut(String cacheName, Object key, Object value) {
                logger.info("trace:{} ,put cache {} {} {}", traceContext.getRootTrace(), cacheName, key, value);
            }

            @Override
            public void onEvict(String cacheName, Object key) {
                logger.info("trace:{} ,evict cache {} {}", traceContext.getRootTrace(), cacheName, key);
            }

            @Override
            public void onClear(String cacheName) {

                logger.info("trace:{} ,clear cache {}", traceContext.getRootTrace(), cacheName);
            }
        };
    }

    private static final Logger sub_test_logger = LoggerFactory.getLogger("sub_test");

    @Bean
    AbstractSubscriber subscriber() {
        return SimpleUseSubscriber.Builder.newBuilder("simple log", (e, p) -> {
            sub_test_logger.info("event id {},event name :{}", e.getEventId(), e.getEventName());
        }).build();
    }

    private static final Logger system_oper_log = LoggerFactory.getLogger("system_oper_log");

    @Bean
    AbstractSubscriber systemLogSub() {
        return SimpleUseSubscriber.Builder.newBuilder("system_oper_log", (event, publisher) -> {
            if (event == null) {
                return;
            }
            var systemOperateRecordEntity = event.getEventDataClass().map(clazz -> {
                if (clazz.equals(SystemOperateRecordEntity.class) && event instanceof SimpleEvent) {
                    SimpleEvent event1 = (SimpleEvent)event;
                    return event1.getDeserializedData();
                }
                return null;
            });
            systemOperateRecordEntity.ifPresent(record -> {
                system_oper_log.info("system operate log {}", record);
            });
        }).supportEventIds(Arrays.asList(EventSystemOpLog.DEFAULT_EVENT_ID)).build();
    }

    @Bean
    ISystemOperateLog systemOperateLog() {
        return new EventSystemOpLog();
    }

    @Bean
    DataHolderCacheManager dataHolderCacheManager() {
        return new DataHolderCacheManager();
    }

    private static final Logger timingWheelScheduleLogger = LoggerFactory.getLogger(TimingWheelSchedule.class);

    @Bean
    TimingWheelSchedule timingWheelSchedule() {
        return new TimingWheelSchedule(new IScheduleExecuteErrorHandle() {
            @Override
            public void handle(IScheduleTask task, Throwable throwable, ISchedule schedule) {
                timingWheelScheduleLogger.error("处理任务时产生错误", throwable);
            }
        }, 5L, TimeUnit.SECONDS, 1L, 60 * 5, TimeUnit.SECONDS);
    }
}
