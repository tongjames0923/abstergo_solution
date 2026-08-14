package com.tbs.mq.impl;

import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.mq.model.FailedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 空实现的失败消息存储
 * <p>
 * 当容器中不存在真实存储实现（如redis-mod提供的Redis实现）时作为兜底装配，
 * 保证依赖IFailedMessageStore的组件可以正常装配与运行：
 * <ul>
 *     <li>save — 不做持久化，仅记录错误日志提示消息将被丢弃</li>
 *     <li>fetchDue — 永远返回空列表</li>
 *     <li>remove — 无操作</li>
 * </ul>
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class NoopFailedMessageStore implements IFailedMessageStore {

    private static final Logger log = LoggerFactory.getLogger(NoopFailedMessageStore.class);

    /**
     * 保存失败消息记录（空实现，仅告警）
     *
     * @param message 失败消息记录
     */
    @Override
    public void save(FailedMessage message) {
        log.error("未配置IFailedMessageStore实现(如redis-mod), 失败消息无法持久化重试, 消息将被丢弃, queue={}, messageId={}",
            message.getQueueName(), message.getMessageId());
    }

    /**
     * 取出到期记录（空实现，永远为空列表）
     *
     * @param now   到期判定时间点
     * @param limit 单次最大取出条数
     * @return 空列表
     */
    @Override
    public List<FailedMessage> fetchDue(Date now, int limit) {
        return Collections.emptyList();
    }

    /**
     * 删除记录（空实现）
     *
     * @param messageId 记录唯一标识
     */
    @Override
    public void remove(String messageId) {
        // 空实现：无持久化记录可删除
    }

}
