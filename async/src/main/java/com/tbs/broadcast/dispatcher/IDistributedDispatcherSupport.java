package com.tbs.broadcast.dispatcher;

import com.tbs.broadcast.bus.IDistributedBusSupport;

/**
 * 分布式事件分发器支持接口
 * <p>
 * 该接口定义了在分布式环境下处理事件的方法，用于支持跨节点的事件分发处理。
 *
 * @author abstergo
 * @see IDistributedBusSupport
 */
public interface IDistributedDispatcherSupport {
	/**
	 * 处理分布式事件
	 * <p>
	 * 在分布式环境中处理传入的事件值，使用指定的分布式总线支持来完成事件处理。
	 *
	 * @param <T>     事件值的类型参数
	 * @param val     事件值，可以是任何类型的数据
	 * @param support 分布式总线支持实例，用于处理特定类型的事件
	 * @see IDistributedBusSupport
	 */
	<T> void handleEvent(T val, IDistributedBusSupport<T> support);
}
