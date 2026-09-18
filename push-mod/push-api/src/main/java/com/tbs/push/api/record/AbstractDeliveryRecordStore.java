package com.tbs.push.api.record;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.push.constants.PushI18nConstant;
import com.tbs.push.model.DeliveryRecord;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 投递记录存储基类
 * <p>
 * 以模板方法封装追加记录的通用补齐与校验，具体介质实现只需覆盖do钩子方法：
 * <ul>
 *     <li>{@link #append(DeliveryRecord)}前补齐记录唯一标识与投递时间，并校验结果状态必填</li>
 *     <li>{@link #queryByMessageId(String)}对空id与实现返回的null兜底为空列表</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public abstract class AbstractDeliveryRecordStore implements IDeliveryRecordStore {

    /**
     * 追加投递记录（含标识补齐与完整性校验）
     *
     * @param record 投递记录
     */
    @Override
    public final void append(DeliveryRecord record) {
        AssertUtil.isTrue(record == null).i18n(PushI18nConstant.DELIVERY_RECORD_REQUIRED).throwIt();
        AssertUtil.isTrue(record.getResult() == null)
            .i18n(PushI18nConstant.DELIVERY_RECORD_RESULT_REQUIRED).throwIt();
        if (StrUtil.isEmpty(record.getId())) {
            record.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (record.getDeliverTime() == null) {
            record.setDeliverTime(new java.util.Date());
        }
        doAppend(record);
    }

    /**
     * 按消息唯一标识查询投递记录（空id与null均兜底为空列表）
     *
     * @param messageId 消息唯一标识
     * @return 投递记录列表，不为null
     */
    @Override
    public final List<DeliveryRecord> queryByMessageId(String messageId) {
        if (StrUtil.isEmpty(messageId)) {
            return Collections.emptyList();
        }
        List<DeliveryRecord> result = doQueryByMessageId(messageId);
        return result == null ? Collections.emptyList() : result;
    }

    /**
     * 实际的追加记录动作
     *
     * @param record 校验补齐后的投递记录
     */
    protected abstract void doAppend(DeliveryRecord record);

    /**
     * 实际的按消息标识查询动作
     *
     * @param messageId 消息唯一标识
     * @return 投递记录列表
     */
    protected abstract List<DeliveryRecord> doQueryByMessageId(String messageId);

}
