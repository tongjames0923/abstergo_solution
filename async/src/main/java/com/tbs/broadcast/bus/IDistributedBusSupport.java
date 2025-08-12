package com.tbs.broadcast.bus;

import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.common.data.IDataBridge;

/**
 * 分布式事件总线支持接口
 *
 * @author wangqiang
 *
 */
public interface IDistributedBusSupport<T> {

	/**
	 * 广播事件发送到分布式系统中
	 *
	 * @param result
	 */
	public void broadcastEvent(T result);

	/**
	 * 获取当前节点的ID
	 *
	 * @return
	 */
	String nodeId();

	/**
	 * 从分布式系统中获取接收事件
	 *
	 * @param raw
	 */
	public void receiveEvent(T data);

	/**
	 * -- GETTER -- 获取数据桥接器
	 *
	 * @return
	 */
	IDataBridge<T, IPublishResult> getDataBridge();

}
