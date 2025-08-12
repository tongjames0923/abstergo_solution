package com.tbs.redis.boradcast;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.tbs.broadcast.bus.IEventBus;
import com.tbs.broadcast.support.AbstractDistributedSupport;
import com.tbs.redis.constants.RedisBeanNameConstants;

import lombok.Getter;

public abstract class AbstractRedisEventSupport<T> extends AbstractDistributedSupport<T> implements MessageListener {

	@Resource(name = RedisBeanNameConstants.DEFAULT_REDIS_TEMPLATE)
	private RedisTemplate<String, Object> redisTemplate;

	/**
	 * -- GETTER -- 获取消息监听容器
	 *
	 * @return
	 */
	@Getter
	@Resource
	private RedisMessageListenerContainer redisMessageListenerContainer;

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
		redisMessageListenerContainer.addMessageListener(this, topic);
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		T obj = (T) redisTemplate.getValueSerializer().deserialize(message.getBody());
		getEventBus().getDistributedBusSupport().ifPresentOrElse(support -> {
			support.receiveEvent(obj);
		}, () -> {
			getEventBus().inqueueEvent(unbridge(obj));
		});
	}

	@Override
	public void broadcastEvent(T result) {
		redisTemplate.convertAndSend(getChannel(), result);
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
