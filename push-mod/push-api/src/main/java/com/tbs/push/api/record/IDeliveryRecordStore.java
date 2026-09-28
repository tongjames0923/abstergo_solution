package com.tbs.push.api.record;

import com.tbs.push.model.message.DeliveryRecord;

import java.util.List;

/**
 * 投递记录存储契约
 * <p>
 * 投递留痕的持久化抽象：分发器对每一次投递动作（含无目标场景）追加一条记录，
 * 保证投递行为完整可审计。实现可以是内存（默认兜底）、数据库、Redis等任意介质。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IDeliveryRecordStore {

    /**
     * 追加投递记录
     *
     * @param record 投递记录
     */
    void append(DeliveryRecord record);

    /**
     * 按消息唯一标识查询投递记录
     *
     * @param messageId 消息唯一标识
     * @return 投递记录列表，不为null（无记录时为空列表）
     */
    List<DeliveryRecord> queryByMessageId(String messageId);

}
