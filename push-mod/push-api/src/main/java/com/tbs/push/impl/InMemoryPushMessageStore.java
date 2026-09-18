package com.tbs.push.impl;

import com.tbs.push.api.store.AbstractPushMessageStore;
import com.tbs.push.model.PushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 内存消息源（默认兜底实现）
 * <p>
 * 基于容量受限的无锁双端队列实现"取出即占用"的投递一次语义：
 * <ul>
 *     <li>入源：容量满时淘汰最早入源的消息并告警，保证进程不被消息堆积拖垮</li>
 *     <li>取出：逐条原子poll，同一条消息只会被取走一次</li>
 *     <li>标记分发：取出即占用，本实现空操作</li>
 * </ul>
 * 进程重启后消息丢失，需跨重启留痕的业务请注册持久化实现（如Redis）覆盖本兜底。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class InMemoryPushMessageStore extends AbstractPushMessageStore {

    private static final Logger log = LoggerFactory.getLogger(InMemoryPushMessageStore.class);

    /**
     * 默认容量上限
     */
    public static final int DEFAULT_CAPACITY = 10_000;

    /**
     * 待分发消息队列
     */
    private final ConcurrentLinkedDeque<PushMessage> queue = new ConcurrentLinkedDeque<>();

    /**
     * 容量上限
     */
    private final int capacity;

    /**
     * 构造内存消息源（默认容量）
     */
    public InMemoryPushMessageStore() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * 构造内存消息源
     *
     * @param capacity 容量上限，非正数按默认容量处理
     */
    public InMemoryPushMessageStore(int capacity) {
        this.capacity = capacity > 0 ? capacity : DEFAULT_CAPACITY;
    }

    /**
     * 实际的入源动作（容量满时淘汰最早消息）
     *
     * @param message 校验补齐后的业务消息
     */
    @Override
    protected void doEnqueue(PushMessage message) {
        while (queue.size() >= capacity) {
            PushMessage dropped = queue.pollFirst();
            if (dropped != null) {
                log.warn("内存消息源已满, 淘汰最早消息, messageId:{}, topic:{}", dropped.getId(), dropped.getTopic());
            }
        }
        queue.addLast(message);
    }

    /**
     * 实际的取出动作（逐条原子poll，取出即占用）
     *
     * @param limit 单次最大取出条数
     * @return 待分发消息列表
     */
    @Override
    protected List<PushMessage> doFetchPending(int limit) {
        List<PushMessage> result = new ArrayList<>(Math.min(limit, queue.size()));
        for (int i = 0; i < limit; i++) {
            PushMessage message = queue.pollFirst();
            if (message == null) {
                break;
            }
            result.add(message);
        }
        return result;
    }

    /**
     * 实际的分发完成标记动作（取出即占用，空操作）
     *
     * @param messageId 消息唯一标识
     */
    @Override
    protected void doMarkDispatched(String messageId) {
        // 空实现：取出即占用，无待确认的分发状态
    }

    /**
     * 统计待分发消息数量
     *
     * @return 待分发消息数量
     */
    @Override
    public int pendingCount() {
        return queue.size();
    }

}
