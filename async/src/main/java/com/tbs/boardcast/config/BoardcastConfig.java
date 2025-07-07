package com.tbs.boardcast.config;

import com.tbs.boardcast.bus.IEventBus;
import com.tbs.boardcast.bus.impl.SimpleEventBus;
import com.tbs.boardcast.config.property.BoardcastProperty;
import com.tbs.boardcast.dispatcher.IEventDispatcher;
import com.tbs.boardcast.dispatcher.impl.SimpleEventDispatcher;
import com.tbs.common.util.BeanUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
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
    @ConditionalOnMissingBean(IEventDispatcher.class)
    public IEventDispatcher dispatcher() {
        return new SimpleEventDispatcher(Executors.newSingleThreadExecutor());
    }

}
