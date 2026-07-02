package com.tbs.broadcast.rabbitmq.support;

import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.broadcast.publisher.impl.SimpleDistributedAcceptedPublishResult;
import com.tbs.broadcast.publisher.impl.SpringManagedListenablePublishResult;
import org.jetbrains.annotations.NotNull;

/**
 * 简单RabbitMQ广播支持实现类
 * <p>
 * 继承自AbstractRabbitMqSupport，提供基于RabbitMQ的简单事件广播和接收功能
 * 实现了事件对象的桥接转换功能
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class SimpleRabbitMqSupport extends AbstractRabbitMqSupport<SimpleDistributedAcceptedPublishResult> {
	/**
	 * 将IPublishResult桥接转换为SimpleDistributedAcceptedPublishResult
	 * <p>
	 * 实现事件对象的桥接转换，用于在分布式环境中传输事件
	 * </p>
	 *
	 * @param obj 要桥接转换的发布结果对象
	 * @return 转换后的SimpleDistributedAcceptedPublishResult对象
	 */
	@Override
	public SimpleDistributedAcceptedPublishResult bridge(@NotNull IPublishResult obj) {
		return new SimpleDistributedAcceptedPublishResult(obj.getEvent(), obj.getPublisher().getPushlisherId(),
				obj.getHandleMode());
	}

	/**
	 * 将SimpleDistributedAcceptedPublishResult解桥转换为IPublishResult
	 * <p>
	 * 实现事件对象的解桥转换，用于在接收端处理事件
	 * </p>
	 *
	 * @param obj 要解桥转换的分布式接受发布结果对象
	 * @return 转换后的IPublishResult对象
	 */
	@Override
	public IPublishResult unbridge(@NotNull SimpleDistributedAcceptedPublishResult obj) {
		return new SpringManagedListenablePublishResult(obj.getEvent(), obj.getPublisher(), obj.getHandleMode());
	}
}
