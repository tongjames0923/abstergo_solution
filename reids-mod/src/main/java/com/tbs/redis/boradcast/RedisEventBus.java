package com.tbs.redis.boradcast;

import com.tbs.boardcast.bus.AbstractSemaphoreEventBus;
import com.tbs.boardcast.enums.EventStatusEnum;
import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.redis.constants.RedisBeanNameConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Redis事件总线
 *
 * @author tongj
 */
@Slf4j
public class RedisEventBus extends AbstractSemaphoreEventBus<IPublishResult> implements MessageListener {

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

    @Resource(name = RedisBeanNameConstants.REDIS_VALUE_SERIALIZER)
    private RedisSerializer redisSerializer;

    private static final String EVENT_CHANNEL = "tbs:boardcast:event_channel";

    private Queue<IPublishResult> localQueue = new ConcurrentLinkedQueue<>();
    /**
     * -- GETTER -- 获取频道主题
     *
     * @return
     */
    @Getter
    private ChannelTopic topic = new ChannelTopic(EVENT_CHANNEL);

    @Override
    protected Queue<IPublishResult> getQueue() {
        return localQueue;
    }

    @Override
    protected <T> IPublishResult put(IPublishResult<T> r) {
        return r;
    }

    @PostConstruct
    public void initMessageListenerContainer() {
        redisMessageListenerContainer.addMessageListener(this, topic);
    }

    public static <T> void preInqueueCheck(IPublishResult<T> publishResult) {
        AssertUtil.notNull(publishResult, "publishResult can not be null");
        AssertUtil.notNull(publishResult.getEvent(), "event can not be null");
        if (publishResult.getEventStatus() == null ||
            publishResult.getEventStatus() == EventStatusEnum.IDLE ||
            publishResult.getEventStatus() == EventStatusEnum.REQUIRE_RETRY) {
            publishResult.setEventStatus(EventStatusEnum.IN_QUEUE);
        } else {
            AssertUtil.throwError(
                () -> new FormattedRuntimeException("事件状态错误,入队的状态仅允许IDLE，REQUIRE_RETRY,IN_QUEUE"));
        }
    }

    @Override
    public <T> void inqueueEvent(IPublishResult<T> publishResult) {
        preInqueueCheck(publishResult);

        // 直接通过Redis Pub/Sub发布事件
        redisTemplate.convertAndSend(EVENT_CHANNEL, publishResult);
    }

    /**
     * 添加事件到本地队列（供消息监听器使用）
     *
     * @param publishResult
     */
    public void addToLocalQueue(IPublishResult publishResult) {
        super.inqueueEvent(publishResult);
    }

    /**
     * 为Redis Pub/Sub模式，该方法不适用 Redis Pub/Sub是推送模式，不需要主动获取事件
     */
    @Override
    public IPublishResult<?> nextUnhandledEvent(Duration duration) {
        // 在Pub/Sub模式下，事件是推送的，不是拉取的
        // 这里仅为了满足接口要求，提供本地队列支持
        return super.nextUnhandledEvent(duration);
    }

    @Override
    public int getEventQueueSize() {
        return super.getEventQueueSize();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 反序列化消息
            Object obj = redisSerializer.deserialize(message.getBody());
            if (obj instanceof IPublishResult) {
                IPublishResult publishResult = (IPublishResult)obj;
                // 添加到本地队列供处理
                addToLocalQueue((IPublishResult)obj);
            } else {
                log.warn("消息格式错误");
            }
        } catch (Exception e) {
            log.error("消息处理错误", e);
        }
    }
}
