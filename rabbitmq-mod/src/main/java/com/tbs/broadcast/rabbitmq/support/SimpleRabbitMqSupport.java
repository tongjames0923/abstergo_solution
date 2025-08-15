package com.tbs.broadcast.rabbitmq.support;

import org.jetbrains.annotations.NotNull;

import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.broadcast.publisher.impl.SimpleDistributedAcceptedPublishResult;
import com.tbs.broadcast.publisher.impl.SpringManagedListenablePublishResult;

public class SimpleRabbitMqSupport extends AbstractRabbaitMqSupport<SimpleDistributedAcceptedPublishResult> {
	@Override
	public SimpleDistributedAcceptedPublishResult bridge(@NotNull IPublishResult obj) {
		return new SimpleDistributedAcceptedPublishResult(obj.getEvent(), obj.getPublisher().getPushlisherId(),
				obj.getHandleMode());
	}

	@Override
	public IPublishResult unbridge(@NotNull SimpleDistributedAcceptedPublishResult obj) {
		return new SpringManagedListenablePublishResult(obj.getEvent(), obj.getPublisher(), obj.getHandleMode());
	}
}
