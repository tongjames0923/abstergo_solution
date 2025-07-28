package com.tbs.app.config;

import com.tbs.boardcast.dispatcher.IEventDispatcher;
import com.tbs.boardcast.event.IEvent;
import com.tbs.boardcast.publisher.IPublisher;
import com.tbs.boardcast.subscriber.AbstractSubscriber;
import com.tbs.cache.core.ICacheListenner;
import com.tbs.common.error.IErrorThrowEvent;
import com.tbs.web.trace.core.AbstractTraceContext;
import com.tbs.web.trace.core.IMethodInvokeTreeProvider;
import com.tbs.web.trace.core.TraceObject;
import com.tbs.web.trace.tool.ITraceContextListenner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutorService;

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

    @Bean
    AbstractSubscriber subscriber(IEventDispatcher dispatcher) {
        return new AbstractSubscriber() {

            private Logger logger = LoggerFactory.getLogger(AppConfig.class);

            @PostConstruct
            public void init() {
                logger.info("init subscriber");
                dispatcher.register(this);
            }

            @Override
            public String getSubscriberId() {
                return "test";
            }

            @Override
            public boolean supportEvent(String eventId, String eventName) {
                return true;
            }

            @Override
            public boolean supportPublisher(IPublisher publisher) {
                return true;
            }

            @Override
            protected void onReceive(IEvent<?> event, IPublisher publisher) {
                try {
                    Thread.currentThread().join(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.info("receive event {} {}", event.getEventName(), event.getEventId());
            }
        };
    }
}
