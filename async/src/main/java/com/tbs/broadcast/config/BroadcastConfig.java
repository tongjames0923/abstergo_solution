package com.tbs.broadcast.config;

import javax.annotation.Resource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tbs.broadcast.bus.IEventBus;
import com.tbs.broadcast.bus.impl.SimpleEventBus;
import com.tbs.broadcast.config.property.BroadcastProperty;
import com.tbs.broadcast.constant.BroadcastBeanNameConstant;
import com.tbs.broadcast.dispatcher.IEventDispatcher;
import com.tbs.broadcast.dispatcher.impl.SimpleEventDispatcher;
import com.tbs.broadcast.publisher.IPublisher;
import com.tbs.broadcast.publisher.impl.DispatcherImportedPublisher;
import com.tbs.common.util.BeanUtil;

/**
 * 广播模块配置类
 * <p>
 * 该类负责配置广播模块的核心组件，包括事件总线、事件分发器和默认发布者等Bean的创建和管理。
 * 通过Spring的条件注解实现组件的自动配置，支持根据环境中是否已存在相应组件来决定是否创建默认实现。
 *
 * @author abstergo
 */
@Configuration
public class BroadcastConfig {

	/**
	 * 广播配置属性，用于读取和管理广播模块的相关配置项
	 */
	@Resource
	private BroadcastProperty broadcastProperty;

	/**
	 * 创建事件总线Bean
	 * <p>
	 * 根据配置属性中的事件总线类型创建对应的事件总线实例，默认使用SimpleEventBus实现。 支持通过配置文件指定不同的事件总线实现类。
	 *
	 * @return 事件总线实例
	 */
	@Bean
	public IEventBus eventBus() {
		return BeanUtil.buildBeanFromProperties(() -> {
			return new SimpleEventBus();
		}, broadcastProperty.getEventBusType(), BeanUtil::buildByEmptyArgs);
	}

	/**
	 * 创建事件分发器Bean
	 * <p>
	 * 当环境中不存在其他IEventDispatcher实现时，创建默认的事件分发器实例。 该分发器实现了基本的异常处理逻辑，将异常信息记录到日志中。
	 *
	 * @return 事件分发器实例
	 */
	@Bean
	@ConditionalOnMissingBean(IEventDispatcher.class)
	public IEventDispatcher dispatcher() {
		return BeanUtil.buildBeanFromProperties(SimpleEventDispatcher::new, broadcastProperty.getEventDispatcherType(),
				BeanUtil::buildByEmptyArgs);
	}

	/**
	 * 创建默认发布者Bean
	 * <p>
	 * 当环境中不存在其他IPublisher实现但存在IEventDispatcher时，创建默认的发布者实例。
	 * 默认发布者基于指定的事件分发器创建，使用预定义的默认发布者ID。
	 *
	 * @param eventDispatcher 事件分发器实例
	 * @return 默认发布者实例
	 */
	@Bean
	@ConditionalOnMissingBean(IPublisher.class)
	@ConditionalOnBean(IEventDispatcher.class)
	IPublisher defaultPublisher(IEventDispatcher eventDispatcher) {
		return new DispatcherImportedPublisher(BroadcastBeanNameConstant.DEFAULT_PUBLISHER_ID, eventDispatcher);
	}

}
