package com.tbs.mq.api.retry;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.mq.constants.MqI18nConstant;
import com.tbs.mq.model.FailedMessage;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 失败消息存储基类
 * <p>
 * 以模板方法封装通用校验与空值兜底：
 * <ul>
 *     <li>{@link #save(FailedMessage)}前校验记录完整性（id与下次重试时间必填）</li>
 *     <li>{@link #fetchDue(Date, int)}对实现返回的null兜底为空列表</li>
 *     <li>{@link #remove(String)}对空id静默跳过</li>
 * </ul>
 * 具体介质（Redis/数据库等）的实现只需覆盖三个do钩子方法。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public abstract class AbstractFailedMessageStore implements IFailedMessageStore {

    /**
     * 保存失败消息记录（含完整性校验）
     *
     * @param message 失败消息记录
     */
    @Override
    public final void save(FailedMessage message) {
        AssertUtil.isTrue(message == null)
            .i18n(MqI18nConstant.FAILED_MESSAGE_REQUIRED)
            .throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(message.getId()))
            .i18n(MqI18nConstant.FAILED_MESSAGE_ID_REQUIRED)
            .throwIt();
        AssertUtil.isTrue(message.getNextRetryTime() == null)
            .i18n(MqI18nConstant.FAILED_MESSAGE_NEXT_RETRY_TIME_REQUIRED)
            .params(message.getId())
            .throwIt();
        doSave(message);
    }

    /**
     * 取出已到期的失败消息记录（null兜底为空列表）
     *
     * @param now   到期判定时间点
     * @param limit 单次最大取出条数
     * @return 已到期的失败消息记录列表，不为null
     */
    @Override
    public final List<FailedMessage> fetchDue(Date now, int limit) {
        List<FailedMessage> result = doFetchDue(now, limit);
        return result == null ? Collections.emptyList() : result;
    }

    /**
     * 删除记录（空id静默跳过）
     *
     * @param messageId 记录唯一标识
     */
    @Override
    public final void remove(String messageId) {
        if (StrUtil.isEmpty(messageId)) {
            return;
        }
        doRemove(messageId);
    }

    /**
     * 实际的保存动作
     *
     * @param message 校验通过的失败消息记录
     */
    protected abstract void doSave(FailedMessage message);

    /**
     * 实际的取出到期记录动作
     * <p>
     * 实现必须保证"取出即锁定"的原子语义。
     * </p>
     *
     * @param now   到期判定时间点
     * @param limit 单次最大取出条数
     * @return 已到期的失败消息记录列表
     */
    protected abstract List<FailedMessage> doFetchDue(Date now, int limit);

    /**
     * 实际的删除动作
     *
     * @param messageId 记录唯一标识
     */
    protected abstract void doRemove(String messageId);

}
