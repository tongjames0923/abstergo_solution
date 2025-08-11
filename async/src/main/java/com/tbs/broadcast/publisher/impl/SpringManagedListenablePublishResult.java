package com.tbs.broadcast.publisher.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.event.IEvent;
import com.tbs.broadcast.publisher.AbstractPublishResult;
import com.tbs.broadcast.publisher.IEventStatusListenner;
import com.tbs.broadcast.publisher.IPublisher;
import com.tbs.common.util.SpringUtil;

/**
 * Spring管理的可监听发布结果实现类
 * <p>
 * 该类是AbstractPublishResult的具体实现，专门用于Spring环境中。
 * 它通过Spring容器获取事件状态监听器和发布者实例，并提供了缓存机制来提高发布者获取的性能。 该类实现了Serializable接口，支持序列化操作。
 *
 * @author tongj
 */
public class SpringManagedListenablePublishResult extends AbstractPublishResult implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 发布者缓存映射表
	 * <p>
	 * 用于缓存已获取的发布者实例，避免重复从Spring容器中获取，提高性能。 key为发布者ID，value为对应的发布者实例。
	 */
	private static final Map<String, IPublisher> pubMap = new ConcurrentHashMap<>(8);

	/**
	 * 构造函数
	 *
	 * @param event      事件对象，不能为null
	 * @param publisher  发布者ID，不能为空
	 * @param handleMode 处理模式（同步或异步），不能为null
	 */
	public SpringManagedListenablePublishResult(IEvent event, String publisher, HandleMode handleMode) {
		super(event, publisher, handleMode);
	}

	/**
	 * 默认构造函数
	 */
	public SpringManagedListenablePublishResult() {
		super();
	}

	/**
	 * 获取事件状态监听器集合
	 * <p>
	 * 通过Spring容器获取所有类型为IEventStatusListenner的Bean实例， 当事件状态变更或处理异常时会通知这些监听器。
	 *
	 * @return Spring容器中的所有事件状态监听器实例集合
	 */
	@Override
	protected Collection<IEventStatusListenner> getStatusListeners() {
		return SpringUtil.getBeansOfType(IEventStatusListenner.class).values();
	}

	/**
	 * 根据发布者ID获取对应的发布者实例
	 * <p>
	 * 首先从本地缓存中查找发布者，如果找不到则从Spring容器中获取所有发布者Bean，
	 * 然后根据ID匹配找到对应的发布者实例并缓存起来。如果未找到匹配的发布者则抛出异常。
	 *
	 * @param publisherId 发布者唯一标识
	 * @return 对应的发布者实例
	 * @throws RuntimeException 当未找到对应ID的发布者时抛出
	 */
	@Override
	protected IPublisher getPublisherById(String publisherId) {
		return pubMap.computeIfAbsent(publisherId, (k) -> {
			return SpringUtil.getBeansOfType(IPublisher.class).values().stream()
					.filter(p -> p != null && publisherId.equals(p.getPushlisherId())).findFirst()
					.orElseThrow(() -> new RuntimeException("未找到发布者:" + publisherId));
		});
	}
}
