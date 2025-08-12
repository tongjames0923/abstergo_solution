package com.tbs.broadcast.support;

import com.tbs.broadcast.bus.IDistributedBusSupport;
import com.tbs.broadcast.dispatcher.IDistributedDispatcherSupport;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.common.data.IDataBridge;

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
