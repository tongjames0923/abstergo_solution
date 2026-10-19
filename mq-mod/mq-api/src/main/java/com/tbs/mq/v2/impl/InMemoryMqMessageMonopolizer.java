package com.tbs.mq.v2.impl;

import com.tbs.mq.v2.core.IMqContext;
import com.tbs.mq.v2.core.IMqMessageMonopolizer;
import com.tbs.mq.v2.vo.MqMessageVO;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息单飞抢占器的内存基本实现（v2 记录驱动模型）
 * <p>
 * 以"上下文ID + messageId"为占有键，用 {@link ConcurrentHashMap#keySet()} 的原子 {@code add} 实现单飞：{@link #monopolize} 仅在键首次占有成功时返回
 * {@code true}，他人已持有返回 {@code false}； {@link #release} 移除键交还占有权。同进程内即可保证同一消息不被并行重处理。
 * </p>
 * <p>
 * 定位为单机/开发/自检用实现，进程重启后占有关系清空；集群环境的跨实例单飞应替换为分布式锁实现 （如基于 Redis 的带租约抢占）。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
public class InMemoryMqMessageMonopolizer implements IMqMessageMonopolizer {

    /**
     * 当前被占有的消息键集合，键为 {@code contextId#messageId}
     */
    private final Set<String> heldKeys = ConcurrentHashMap.newKeySet();

    /**
     * 抢占消息：同一上下文下的同一 messageId 仅允许一个持有者
     *
     * @param context 当前上下文（队列级作用域）
     * @param message 待抢占的消息主记录VO
     * @param <R>     厂商原始消息类型
     * @param <T>     业务负载类型
     * @return true 表示抢占成功，调用方负责处理完成后 release；false 表示他人持有
     */
    @Override
    public <R, T extends Serializable> boolean monopolize(IMqContext<R, T> context, MqMessageVO<R, T> message) {
        if (message == null || message.getMessageId() == null) {
            return false;
        }
        return heldKeys.add(buildKey(context, message.getMessageId()));
    }

    /**
     * 释放消息的占有，仅限抢占成功的持有者调用
     *
     * @param context 当前上下文（队列级作用域）
     * @param message 已分发完成的消息主记录VO
     * @param <R>     厂商原始消息类型
     * @param <T>     业务负载类型
     * @return 释放后的消息主记录VO（原样返回）
     */
    @Override
    public <R, T extends Serializable> MqMessageVO<R, T> release(IMqContext<R, T> context, MqMessageVO<R, T> message) {
        if (message != null && message.getMessageId() != null) {
            heldKeys.remove(buildKey(context, message.getMessageId()));
        }
        return message;
    }

    /**
     * 构造占有键：上下文ID 与 messageId 组合，隔离不同队列下同 ID 的归属
     *
     * @param context   当前上下文
     * @param messageId 消息ID
     * @return 占有键
     */
    private static String buildKey(IMqContext<?, ?> context, String messageId) {
        Long contextId = context == null ? 0L : context.getContextId();
        return contextId + "#" + messageId;
    }
}
