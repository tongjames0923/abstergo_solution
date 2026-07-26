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
 * 简单的Redis事件支持实现类
 * <p>
 * 继承自AbstractRedisEventSupport，实现基于Redis的事件广播和监听功能
 * 使用SimpleDistributedAcceptedPublishResult作为事件类型
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class SimpleRedisEventSupport extends AbstractRedisEventSupport<SimpleDistributedAcceptedPublishResult> {

	@Resource
	@Lazy
	private IEventDispatcher dispatcher;

	/**
	 * 获取事件总线
	 * <p>
	 * 从事件调度器中获取事件总线实例
	 * </p>
	 *
	 * @return 事件总线实例
	 */
	@Override
	protected IEventBus getEventBus() {
		return dispatcher.getEventBus();
	}

	/**
	 * 获取节点ID
	 * <p>
	 * 使用频道名称作为节点ID
	 * </p>
	 *
	 * @return 节点ID
	 */
	@Override
	public String nodeId() {
		return getChannel();
	}

	/**
	 * 处理事件
	 * <p>
	 * 从分布式总线支持中获取数据桥接器，解桥接后将事件传递给调度器处理
	 * </p>
	 *
	 * @param val     事件值
	 * @param support 分布式总线支持
	 * @param <T>     事件类型
	 */
	@Override
	public <T> void handleEvent(T val, IDistributedBusSupport<T> support) {
		dispatcher.handleEvent(support.getDataBridge().unbridge(val));
	}

	/**
	 * 桥接IPublishResult到SimpleDistributedAcceptedPublishResult
	 * <p>
	 * 使用内部的SimpleDataBridge实现桥接转换
	 * </p>
	 *
	 * @param obj IPublishResult实例
	 * @return SimpleDistributedAcceptedPublishResult实例
	 */
	@Override
	public SimpleDistributedAcceptedPublishResult bridge( IPublishResult obj) {
		return simpleDataBridge.bridge(obj);
	}

	/**
	 * 解桥接SimpleDistributedAcceptedPublishResult到IPublishResult
	 * <p>
	 * 使用内部的SimpleDataBridge实现解桥接转换
	 * </p>
	 *
	 * @param obj SimpleDistributedAcceptedPublishResult实例
	 * @return IPublishResult实例
	 */
	@Override
	public IPublishResult unbridge( SimpleDistributedAcceptedPublishResult obj) {
		return simpleDataBridge.unbridge(obj);
	}

	/**
	 * 获取事件广播频道
	 * <p>
	 * 返回固定的频道名称"tbs.event.channel"
	 * </p>
	 *
	 * @return 事件广播频道名称
	 */
	@Override
	public String getChannel() {
		return "tbs.event.channel";
	}

	/**
	 * 简单的数据桥接器内部类
	 * <p>
	 * 实现IDataBridge接口，用于IPublishResult和SimpleDistributedAcceptedPublishResult之间的转换
	 * </p>
	 */
	private static class SimpleDataBridge
			implements IDataBridge<SimpleDistributedAcceptedPublishResult, IPublishResult> {

		/**
		 * 桥接IPublishResult到SimpleDistributedAcceptedPublishResult
		 *
		 * @param obj IPublishResult实例
		 * @return SimpleDistributedAcceptedPublishResult实例
		 */
		@Override
		public SimpleDistributedAcceptedPublishResult bridge( IPublishResult obj) {
			return new SimpleDistributedAcceptedPublishResult(obj.getEvent(), obj.getPublisher().getPushlisherId(),
				obj.getHandleMode());
		}

		/**
		 * 解桥接SimpleDistributedAcceptedPublishResult到IPublishResult
		 *
		 * @param obj SimpleDistributedAcceptedPublishResult实例
		 * @return IPublishResult实例
		 */
		@Override
		public IPublishResult unbridge( SimpleDistributedAcceptedPublishResult obj) {
			return new SpringManagedListenablePublishResult(obj.getEvent(), obj.getPublisher(), obj.getHandleMode());
		}
	}

	private static final SimpleDataBridge simpleDataBridge = new SimpleDataBridge();

}
