package com.tbs.push.api.store;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.push.constants.PushI18nConstant;
import com.tbs.push.model.message.PushMessage;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 推送消息源存储基类
 * <p>
 * 以模板方法封装入源的通用补齐与校验，具体介质实现只需覆盖do钩子方法：
 * <ul>
 *     <li>{@link #enqueue(PushMessage)}前补齐消息唯一标识（UUID去连字符）与入库时间，
 *     并校验topic必填</li>
 *     <li>{@link #fetchPending(int)}对实现返回的null兜底为空列表</li>
 *     <li>{@link #markDispatched(String)}对空id静默跳过</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public abstract class AbstractPushMessageStore implements IPushMessageStore {

    /**
     * 消息入源（含标识补齐与完整性校验）
     *
     * @param message 业务消息
     */
    @Override
    public final void enqueue(PushMessage message) {
        AssertUtil.isTrue(message == null).i18n(PushI18nConstant.MESSAGE_REQUIRED).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(message.getTopic()))
            .i18n(PushI18nConstant.MESSAGE_TOPIC_REQUIRED).throwIt();
        if (StrUtil.isEmpty(message.getId())) {
            message.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (message.getEnqueueTime() == null) {
            message.setEnqueueTime(new java.util.Date());
        }
        doEnqueue(message);
    }

    /**
     * 原子取出一批待分发消息（null兜底为空列表）
     *
     * @param limit 单次最大取出条数
     * @return 待分发消息列表，不为null
     */
    @Override
    public final List<PushMessage> fetchPending(int limit) {
        List<PushMessage> result = doFetchPending(limit);
        return result == null ? Collections.emptyList() : result;
    }

    /**
     * 标记消息已分发完成（空id静默跳过）
     *
     * @param messageId 消息唯一标识
     */
    @Override
    public final void markDispatched(String messageId) {
        if (StrUtil.isEmpty(messageId)) {
            return;
        }
        doMarkDispatched(messageId);
    }

    /**
     * 实际的入源动作
     *
     * @param message 校验补齐后的业务消息
     */
    protected abstract void doEnqueue(PushMessage message);

    /**
     * 实际的原子取出动作
     * <p>
     * 实现必须保证"取出即占用"的原子语义。
     * </p>
     *
     * @param limit 单次最大取出条数
     * @return 待分发消息列表
     */
    protected abstract List<PushMessage> doFetchPending(int limit);

    /**
     * 实际的分发完成标记动作
     *
     * @param messageId 消息唯一标识
     */
    protected abstract void doMarkDispatched(String messageId);

}
