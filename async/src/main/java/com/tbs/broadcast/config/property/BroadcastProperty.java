package com.tbs.broadcast.config.property;

import com.tbs.broadcast.bus.IEventBus;
import com.tbs.broadcast.dispatcher.IEventDispatcher;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author abstergo
 */
@Component
@ConfigurationProperties(prefix = "tbs.boardcast")
@Data
public class BroadcastProperty {

    /**
     * 默认事件总线
     */
    private Class<? extends IEventBus> eventBusType = com.tbs.broadcast.bus.impl.SimpleEventBus.class;

    /**
     * 默认事件分发器
     */
    private Class<? extends IEventDispatcher> eventDispatcherType;

}
