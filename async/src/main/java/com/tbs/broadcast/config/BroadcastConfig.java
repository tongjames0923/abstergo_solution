package com.tbs.broadcast.config;

import com.tbs.broadcast.bus.IEventBus;
import com.tbs.broadcast.bus.impl.SimpleEventBus;
import com.tbs.broadcast.config.property.BroadcastProperty;
import com.tbs.broadcast.constant.BroadcastBeanNameConstant;
import com.tbs.broadcast.dispatcher.IEventDispatcher;
import com.tbs.broadcast.dispatcher.impl.AbstractEventDispatcher;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.broadcast.publisher.IPublisher;
import com.tbs.broadcast.publisher.impl.DispatcherImportedPublisher;
import com.tbs.common.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author abstergo
 */
@Configuration
public class BroadcastConfig {

    @Resource
    private BroadcastProperty broadcastProperty;

    @Bean
    public IEventBus eventBus() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new SimpleEventBus();
        }, broadcastProperty.getEventBusType(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(IEventDispatcher.class)
    public IEventDispatcher dispatcher() {
        return new AbstractEventDispatcher() {
            private final Logger logger = LoggerFactory.getLogger(BroadcastConfig.class);

            @Override
            public <T> void handleError(Throwable throwable, IPublishResult publishResult) {
                logger.error("处理消息时产生错误！！！", throwable);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(IPublisher.class)
    @ConditionalOnBean(IEventDispatcher.class)
    IPublisher defaultPublisher(IEventDispatcher eventDispatcher) {
        return new DispatcherImportedPublisher(BroadcastBeanNameConstant.DEFAULT_PUBLISHER_ID, eventDispatcher);
    }

}
