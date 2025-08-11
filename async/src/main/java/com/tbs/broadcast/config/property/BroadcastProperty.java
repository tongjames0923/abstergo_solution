package com.tbs.broadcast.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.tbs.broadcast.bus.IEventBus;
import com.tbs.broadcast.dispatcher.IEventDispatcher;

import lombok.Data;

/**
 * 广播模块配置属性类
 * <p>
 * 该类用于读取和管理广播模块的配置属性，支持从配置文件中读取相关配置项。
 * 通过@ConfigurationProperties注解绑定前缀为"tbs.boardcast"的配置属性。
 * 使用@Data注解自动生成getter、setter等方法。
 *
 * @author abstergo
 */
@Component
@ConfigurationProperties(prefix = "tbs.boardcast")
@Data
public class BroadcastProperty {

	/**
	 * 默认事件总线类型
	 * <p>
	 * 指定广播模块使用的事件总线实现类，默认使用SimpleEventBus实现。
	 * 可以通过配置文件中的"tbs.boardcast.event-bus-type"属性进行自定义配置。
	 */
	private Class<? extends IEventBus> eventBusType = com.tbs.broadcast.bus.impl.SimpleEventBus.class;

	/**
	 * 默认事件分发器类型
	 * <p>
	 * 指定广播模块使用的事件分发器实现类，默认值为null。
	 * 可以通过配置文件中的"tbs.boardcast.event-dispatcher-type"属性进行自定义配置。
	 */
	private Class<? extends IEventDispatcher> eventDispatcherType;

}
