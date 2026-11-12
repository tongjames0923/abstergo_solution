package com.tbs.push.impl.store;

import com.tbs.push.api.store.AbstractPushMessageStore;
import com.tbs.push.model.message.PushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存消息源（默认兜底实现）
 * <p>
 * 基于容量受限的无锁双端队列实现"取出即占用"的投递一次语义：
 * <ul>
 *     <li>入源：容量满时淘汰最早入源的消息，保证进程不被消息堆积拖垮</li>
 *     <li>取出：逐条原子poll，同一条消息只会被取走一次</li>
 *     <li>标记分发：取出即占用，本实现空操作</li>
 * </ul>
 * 队列长度用 {@link AtomicInteger} 自行计数，不调用 {@code ConcurrentLinkedDeque.size()}
 * （后者是 O(n) 遍历，消息洪峰下每条消息都遍历万级节点会把 CPU 打满）；
 * 淘汰日志按步长汇总，避免容量打满后每条消息刷一条 WARN 拖垮日志 IO。
 * 进程重启后消息丢失，需跨重启留痕的业务请注册持久化实现（如Redis）覆盖本兜底。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class InMemoryPushMessageStore extends AbstractPushMessageStore {

    /**
     * 默认容量上限
     */
    public static final int DEFAULT_CAPACITY = 10_000;

    /**
     * 淘汰日志限流步长：洪峰下每淘汰 N 条打一条汇总日志
     */
    private static final int DROP_LOG_INTERVAL = 1000;

    private static final Logger log = LoggerFactory.getLogger(InMemoryPushMessageStore.class);

    /**
     * 待分发消息队列
     */
    private final ConcurrentLinkedDeque<PushMessage> queue = new ConcurrentLinkedDeque<>();

    /**
     * 当前队列长度（与 queue 并发增减，仅用于容量判定，允许瞬时偏差）
     */
    private final AtomicInteger size = new AtomicInteger();

    /**
     * 累计淘汰条数
     */
    private final AtomicLong droppedCount = new AtomicLong();

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
        while (size.get() >= capacity) {
            PushMessage dropped = queue.pollFirst();
            if (dropped == null) {
                // 队列已被并发取空：计数归零后退出，避免空转死循环
                size.set(0);
                break;
            }
            size.decrementAndGet();
            long total = droppedCount.incrementAndGet();
            if (total == 1 || total % DROP_LOG_INTERVAL == 0) {
                log.warn("内存消息源已满, 累计淘汰={}, 最近淘汰 messageId:{}, topic:{}",
                    total, dropped.getId(), dropped.getTopic());
            }
        }
        queue.addLast(message);
        size.incrementAndGet();
    }

    /**
     * 实际的取出动作（逐条原子poll，取出即占用）
     *
     * @param limit 单次最大取出条数
     * @return 待分发消息列表
     */
    @Override
    protected List<PushMessage> doFetchPending(int limit) {
        List<PushMessage> result = new ArrayList<>(Math.min(limit, Math.max(size.get(), 0)));
        for (int i = 0; i < limit; i++) {
            PushMessage message = queue.pollFirst();
            if (message == null) {
                break;
            }
            size.decrementAndGet();
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
        return Math.max(size.get(), 0);
    }

}
