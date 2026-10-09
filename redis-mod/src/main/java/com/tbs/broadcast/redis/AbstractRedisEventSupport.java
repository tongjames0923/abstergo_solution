package com.tbs.broadcast.redis;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;

import com.tbs.broadcast.bus.IEventBus;
import com.tbs.broadcast.support.AbstractDistributedSupport;

import lombok.Getter;

/**
 * Redis事件广播支持抽象类
 * <p>
 * 继承自AbstractDistributedSupport，提供基于Redis的事件广播和监听功能
 * 使用Redisson客户端实现消息的发布和订阅
 * </p>
 *
 * @param <T> 事件类型泛型
 * @author tongj
 * @since 1.0.0
 */
public abstract class AbstractRedisEventSupport<T> extends AbstractDistributedSupport<T> {

	/**
	 * Redisson客户端实例
	 */
	@Resource
	private RedissonClient redissonClient;

	/**
	 * 获取事件总线
	 * <p>
	 * 抽象方法，由子类实现，返回具体的事件总线实例
	 * </p>
	 *
	 * @return 事件总线实例
	 */
	protected abstract IEventBus getEventBus();

	/**
	 * 构造方法
	 * <p>
	 * 初始化频道主题
	 * </p>
	 */
	public AbstractRedisEventSupport() {
		topic = new ChannelTopic(getChannel());
	}

	/**
	 * 初始化消息监听器容器
	 * <p>
	 * 使用@PostConstruct注解，在bean初始化后执行
	 * 创建Redis主题监听器，接收消息并分发到事件总线
	 * </p>
	 */
	@PostConstruct
	public void initMessageListenerContainer() {
		RTopic topic = redissonClient.getTopic(getChannel());
		topic.addListener(Object.class, new MessageListener<Object>() {
			@Override
			public void onMessage(CharSequence channel, Object msg) {
				T obj = (T) msg;
				getEventBus().getDistributedBusSupport().ifPresentOrElse(support -> {
					support.receiveEvent(obj);
				}, () -> {
					getEventBus().inqueueEvent(unbridge(obj));
				});
			}
		});
	}

	/**
	 * 广播事件
	 * <p>
	 * 将事件发布到Redis主题，实现分布式事件广播
	 * </p>
	 *
	 * @param result 要广播的事件
	 */
	@Override
	public void broadcastEvent(T result) {
		RTopic topic = redissonClient.getTopic(getChannel());
		topic.publish(result);
	}

	/**
	 * 获取频道名称
	 * <p>
	 * 抽象方法，由子类实现，返回事件广播的频道名称
	 * </p>
	 *
	 * @return 频道名称
	 */
	public abstract String getChannel();

	/**
	 * 频道主题
	 */
	@Getter
	private ChannelTopic topic;
}
