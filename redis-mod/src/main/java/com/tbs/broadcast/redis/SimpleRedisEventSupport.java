package com.tbs.broadcast.redis;

import javax.annotation.Resource;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;

import com.tbs.broadcast.bus.IDistributedBusSupport;
import com.tbs.broadcast.bus.IEventBus;
import com.tbs.broadcast.dispatcher.IEventDispatcher;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.broadcast.publisher.impl.SimpleDistributedAcceptedPublishResult;
import com.tbs.broadcast.publisher.impl.SpringManagedListenablePublishResult;
import com.tbs.common.data.IDataBridge;

/**
 * 默认的Redis事件总线实现
 *
 * @author tongj
 */
public class SimpleRedisEventSupport extends AbstractRedisEventSupport<SimpleDistributedAcceptedPublishResult> {

	@Resource
	@Lazy
	private IEventDispatcher dispatcher;

	@Override
	protected IEventBus getEventBus() {
		return dispatcher.getEventBus();
	}

	@Override
	public String nodeId() {
		return getChannel();
	}

	@Override
	public <T> void handleEvent(T val, IDistributedBusSupport<T> support) {
		dispatcher.handleEvent(support.getDataBridge().unbridge(val));
	}

	@Override
	public SimpleDistributedAcceptedPublishResult bridge(@NotNull IPublishResult obj) {
		return simpleDataBridge.bridge(obj);
	}

	@Override
	public IPublishResult unbridge(@NotNull SimpleDistributedAcceptedPublishResult obj) {
		return simpleDataBridge.unbridge(obj);
	}

	@Override
	public String getChannel() {
		return "tbs.event.channel";
	}

	private static class SimpleDataBridge
			implements IDataBridge<SimpleDistributedAcceptedPublishResult, IPublishResult> {

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

	private static final SimpleDataBridge simpleDataBridge = new SimpleDataBridge();

}
