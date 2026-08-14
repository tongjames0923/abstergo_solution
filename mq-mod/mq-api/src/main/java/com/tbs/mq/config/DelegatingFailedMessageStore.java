package com.tbs.mq.config;

import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.mq.impl.NoopFailedMessageStore;
import com.tbs.mq.model.FailedMessage;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Date;
import java.util.List;

/**
 * 失败消息存储懒委托包装
 * <p>
 * 装配阶段容器可能尚未引入真实存储实现（如redis-mod的Redis实现晚于本配置解析），
 * 直接以{@link org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean}
 * 决策会在早期固化Noop兜底。本包装在每次调用时经{@link ObjectProvider}重新解析目标存储：
 * <ul>
 *     <li>存在真实实现（非本包装自身）时全部调用透传</li>
 *     <li>不存在时退化为Noop兜底行为</li>
 * </ul>
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
class DelegatingFailedMessageStore implements IFailedMessageStore {

    /**
     * 存储实现提供者
     */
    private final ObjectProvider<IFailedMessageStore> storeProvider;

    /**
     * 兜底空实现
     */
    private final IFailedMessageStore fallback = new NoopFailedMessageStore();

    /**
     * 构造懒委托包装
     *
     * @param storeProvider 存储实现提供者
     */
    DelegatingFailedMessageStore(ObjectProvider<IFailedMessageStore> storeProvider) {
        this.storeProvider = storeProvider;
    }

    /**
     * 解析实际生效的存储实现
     * <p>
     * 跳过自身避免自引用递归；未找到真实实现时返回Noop兜底。
     * </p>
     *
     * @return 实际存储实现
     */
    private IFailedMessageStore resolve() {
        return storeProvider.stream()
            .filter(store -> store != this)
            .findFirst()
            .orElse(fallback);
    }

    /**
     * 保存失败消息记录（透传实际存储）
     *
     * @param message 失败消息记录
     */
    @Override
    public void save(FailedMessage message) {
        resolve().save(message);
    }

    /**
     * 取出到期记录（透传实际存储）
     *
     * @param now   到期判定时间点
     * @param limit 单次最大取出条数
     * @return 已到期的失败消息记录列表
     */
    @Override
    public List<FailedMessage> fetchDue(Date now, int limit) {
        return resolve().fetchDue(now, limit);
    }

    /**
     * 删除记录（透传实际存储）
     *
     * @param messageId 记录唯一标识
     */
    @Override
    public void remove(String messageId) {
        resolve().remove(messageId);
    }

}
