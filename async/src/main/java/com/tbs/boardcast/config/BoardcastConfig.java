package com.tbs.boardcast.config;

import com.tbs.boardcast.bus.IEventBus;
import com.tbs.boardcast.bus.impl.SimpleEventBus;
import com.tbs.boardcast.config.property.BoardcastProperty;
import com.tbs.boardcast.constant.BeanNameConstant;
import com.tbs.boardcast.dispatcher.IEventDispatcher;
import com.tbs.boardcast.dispatcher.impl.AbstractEventDispatcher;
import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.boardcast.publisher.IPublisher;
import com.tbs.boardcast.publisher.impl.DispatcherImportedPublisher;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.impl.MapDataHolder;
import com.tbs.common.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author abstergo
 */
@Configuration
public class BoardcastConfig {

    @Resource
    private BoardcastProperty boardcastProperty;

    @Bean
    public IEventBus eventBus() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new SimpleEventBus();
        }, boardcastProperty.getEventBusType(), BeanUtil::buildByEmptyArgs);
    }

    @Bean(BeanNameConstant.SUBSCRIBER_HOLDER_BEAN_NAME)
    IDataHolder subscriberContext() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new MapDataHolder();
        }, boardcastProperty.getSubscriberHolderType(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(IEventDispatcher.class)
    public IEventDispatcher dispatcher(
        @Qualifier(BeanNameConstant.SUBSCRIBER_HOLDER_BEAN_NAME) IDataHolder subscriberContext) {
        return new AbstractEventDispatcher(subscriberContext) {
            private final Logger logger = LoggerFactory.getLogger(BoardcastConfig.class);

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
        return new DispatcherImportedPublisher(BeanNameConstant.DEFAULT_PUBLISHER_ID, eventDispatcher);
    }

}
