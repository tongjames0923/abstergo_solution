package com.tbs.broadcast.redis;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;

import com.tbs.broadcast.bus.IEventBus;
import com.tbs.broadcast.support.AbstractDistributedSupport;

import lombok.Getter;

public abstract class AbstractRedisEventSupport<T> extends AbstractDistributedSupport<T> {

	@Resource
	private RedissonClient redissonClient;

	/**
	 * -- GETTER -- 获取事件总线
	 *
	 * @return
	 */
	protected abstract IEventBus getEventBus();

	public AbstractRedisEventSupport() {
		topic = new ChannelTopic(getChannel());
	}

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

	@Override
	public void broadcastEvent(T result) {
		RTopic topic = redissonClient.getTopic(getChannel());
		topic.publish(result);
	}

	/**
	 * 获取频道名称
	 *
	 * @return
	 */
	public abstract String getChannel();

	/**
	 * -- GETTER -- 获取频道主题
	 *
	 * @return
	 */
	@Getter
	private ChannelTopic topic;
}
