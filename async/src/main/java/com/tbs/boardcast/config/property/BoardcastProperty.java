package com.tbs.boardcast.config.property;

import com.tbs.boardcast.bus.IEventBus;
import com.tbs.boardcast.dispatcher.IEventDispatcher;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author abstergo
 */
@Component
@ConfigurationProperties(prefix = "tbs.boardcast")
@Data
public class BoardcastProperty {
    /**
     * 默认事件总线
     */
    private Class<? extends IEventBus> eventBusType = com.tbs.boardcast.bus.impl.SimpleEventBus.class;

    /**
     * 默认事件分发器
     */
    private Class<? extends IEventDispatcher> eventDispatcherType;
}
