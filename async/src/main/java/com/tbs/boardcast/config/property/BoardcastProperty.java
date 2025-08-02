package com.tbs.boardcast.config.property;

import com.tbs.boardcast.bus.IEventBus;
import com.tbs.boardcast.dispatcher.IEventDispatcher;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.impl.MapDataHolder;
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

    /**
     * 默认订阅者持有器
     */
    private Class<? extends IDataHolder> subscriberHolderType = MapDataHolder.class;


}
