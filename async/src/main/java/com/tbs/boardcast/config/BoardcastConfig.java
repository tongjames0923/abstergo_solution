package com.tbs.boardcast.config;

import com.tbs.boardcast.bus.IEventBus;
import com.tbs.boardcast.bus.impl.SimpleEventBus;
import com.tbs.boardcast.config.property.BoardcastProperty;
import com.tbs.boardcast.constant.BeanNameConstant;
import com.tbs.boardcast.dispatcher.IEventDispatcher;
import com.tbs.boardcast.dispatcher.impl.AbstractEventDispatcher;
import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.holder.context.AbstractNamedContext;
import com.tbs.common.holder.context.impl.LocalStoredMapImplContext;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.Executors;

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

    @Bean
    AbstractNamedContext subscriberContext() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new LocalStoredMapImplContext(BeanNameConstant.DEFAULT_SUBSCRIBER_CONTEXT_ID, new HashMap(8));
        }, boardcastProperty.getSubscriberContextType(), (c) -> {
            try {
                return c.getConstructor(String.class).newInstance(BeanNameConstant.DEFAULT_SUBSCRIBER_CONTEXT_ID);
            } catch (Throwable e) {
                AssertUtil.throwError(() -> new FormattedRuntimeException(e,
                    "HolderProperty.subscriberContextType  Error to build bean"));
                return null;
            }
        });
    }

    @Bean
    @ConditionalOnMissingBean(IEventDispatcher.class)
    public IEventDispatcher dispatcher() {
        return new AbstractEventDispatcher(Executors.newSingleThreadExecutor()) {
            private final Logger logger = LoggerFactory.getLogger(AbstractEventDispatcher.class);

            @Override
            public <T> void handleError(Throwable throwable, IPublishResult<T> publishResult) {
                logger.error("处理消息时产生错误！！！", throwable);
            }
        };
    }

}
