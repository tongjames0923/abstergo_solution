package com.tbs.push.impl.store;

import com.tbs.push.api.record.AbstractDeliveryRecordStore;
import com.tbs.push.model.message.DeliveryRecord;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 内存投递记录存储（默认兜底实现）
 * <p>
 * 基于容量受限的队列保留最近的投递记录：容量满时淘汰最早记录，防止长期运行内存膨胀。
 * 长度用 {@link AtomicInteger} 自行计数，避免 {@code ConcurrentLinkedDeque.size()} 的
 * O(n) 遍历在「消息数 × 目标会话数」放大下拖垮 CPU。
 * 记录仅存于进程内存，需跨重启审计的业务请注册持久化实现覆盖本兜底。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class InMemoryDeliveryRecordStore extends AbstractDeliveryRecordStore {

    /**
     * 默认容量上限
     */
    public static final int DEFAULT_CAPACITY = 10_000;

    /**
     * 投递记录队列
     */
    private final ConcurrentLinkedDeque<DeliveryRecord> records = new ConcurrentLinkedDeque<>();

    /**
     * 当前记录数（与 records 并发增减，仅用于容量判定，允许瞬时偏差）
     */
    private final AtomicInteger size = new AtomicInteger();

    /**
     * 容量上限
     */
    private final int capacity;

    /**
     * 构造内存投递记录存储（默认容量）
     */
    public InMemoryDeliveryRecordStore() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * 构造内存投递记录存储
     *
     * @param capacity 容量上限，非正数按默认容量处理
     */
    public InMemoryDeliveryRecordStore(int capacity) {
        this.capacity = capacity > 0 ? capacity : DEFAULT_CAPACITY;
    }

    /**
     * 实际的追加记录动作（容量满时淘汰最早记录）
     *
     * @param record 校验补齐后的投递记录
     */
    @Override
    protected void doAppend(DeliveryRecord record) {
        while (size.get() >= capacity) {
            if (records.pollFirst() == null) {
                size.set(0);
                break;
            }
            size.decrementAndGet();
        }
        records.addLast(record);
        size.incrementAndGet();
    }

    /**
     * 实际的按消息标识查询动作
     *
     * @param messageId 消息唯一标识
     * @return 投递记录列表
     */
    @Override
    protected List<DeliveryRecord> doQueryByMessageId(String messageId) {
        return records.stream().filter(record -> messageId.equals(record.getMessageId())).collect(Collectors.toList());
    }

}
