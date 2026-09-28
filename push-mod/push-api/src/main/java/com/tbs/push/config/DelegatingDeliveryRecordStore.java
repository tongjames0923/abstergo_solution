package com.tbs.push.config;

import com.tbs.push.api.record.IDeliveryRecordStore;
import com.tbs.push.model.message.DeliveryRecord;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

/**
 * 投递记录存储懒委托包装
 * <p>
 * 装配阶段容器可能尚未引入真实记录存储实现，直接以ConditionalOnMissingBean决策
 * 会在早期固化内存兜底。本包装在每次调用时经ObjectProvider重新解析目标存储：
 * <ul>
 *     <li>存在真实实现（非本包装自身）时全部调用透传</li>
 *     <li>不存在时退化为内存兜底行为</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
class DelegatingDeliveryRecordStore implements IDeliveryRecordStore {

    /**
     * 记录存储实现提供者
     */
    private final ObjectProvider<IDeliveryRecordStore> storeProvider;

    /**
     * 内存兜底实现
     */
    private final IDeliveryRecordStore fallback;

    /**
     * 构造懒委托包装
     *
     * @param storeProvider 记录存储实现提供者
     * @param fallback      内存兜底实现
     */
    DelegatingDeliveryRecordStore(ObjectProvider<IDeliveryRecordStore> storeProvider, IDeliveryRecordStore fallback) {
        this.storeProvider = storeProvider;
        this.fallback = fallback;
    }

    /**
     * 解析实际生效的记录存储实现
     *
     * @return 实际记录存储实现
     */
    private IDeliveryRecordStore resolve() {
        return storeProvider.stream().filter(store -> store != this).findFirst().orElse(fallback);
    }

    /**
     * 追加投递记录（透传实际存储）
     *
     * @param record 投递记录
     */
    @Override
    public void append(DeliveryRecord record) {
        resolve().append(record);
    }

    /**
     * 按消息唯一标识查询投递记录（透传实际存储）
     *
     * @param messageId 消息唯一标识
     * @return 投递记录列表
     */
    @Override
    public List<DeliveryRecord> queryByMessageId(String messageId) {
        return resolve().queryByMessageId(messageId);
    }

}
