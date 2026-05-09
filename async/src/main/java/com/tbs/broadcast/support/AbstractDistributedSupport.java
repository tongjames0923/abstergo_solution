package com.tbs.broadcast.support;

import com.tbs.broadcast.bus.IDistributedBusSupport;
import com.tbs.broadcast.dispatcher.IDistributedDispatcherSupport;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.common.data.IDataBridge;

/**
 * 分布式支持抽象类
 * <p>
 * 抽象类，实现IDistributedBusSupport和IDistributedDispatcherSupport接口 提供了分布式事件处理和分发的基础功能
 * </p>
 *
 * @param <T> 事件类型
 * @author tongj
 * @since 1.0.0
 */
public abstract class AbstractDistributedSupport<T>
		implements IDistributedBusSupport<T>, IDistributedDispatcherSupport, IDataBridge<T, IPublishResult> {

	@Override
	public void receiveEvent(T data) {
		handleEvent(data, this);
	}

	@Override
	public IDataBridge<T, IPublishResult> getDataBridge() {
		return this;
	}

}
