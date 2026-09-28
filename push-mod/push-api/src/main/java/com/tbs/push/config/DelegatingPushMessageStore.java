package com.tbs.push.config;

import com.tbs.push.api.store.IPushMessageStore;
import com.tbs.push.model.message.PushMessage;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

/**
 * 推送消息源懒委托包装
 * <p>
 * 装配阶段容器可能尚未引入真实存储实现（如redis-mod提供的Redis实现晚于本配置解析），
 * 直接以ConditionalOnMissingBean决策会在早期固化内存兜底。本包装在每次调用时
 * 经ObjectProvider重新解析目标存储：
 * <ul>
 *     <li>存在真实实现（非本包装自身）时全部调用透传</li>
 *     <li>不存在时退化为内存兜底行为</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
class DelegatingPushMessageStore implements IPushMessageStore {

    /**
     * 存储实现提供者
     */
    private final ObjectProvider<IPushMessageStore> storeProvider;

    /**
     * 内存兜底实现
     */
    private final IPushMessageStore fallback;

    /**
     * 构造懒委托包装
     *
     * @param storeProvider 存储实现提供者
     * @param fallback      内存兜底实现
     */
    DelegatingPushMessageStore(ObjectProvider<IPushMessageStore> storeProvider, IPushMessageStore fallback) {
        this.storeProvider = storeProvider;
        this.fallback = fallback;
    }

    /**
     * 解析实际生效的存储实现
     * <p>
     * 跳过自身避免自引用递归；未找到真实实现时返回内存兜底。
     * </p>
     *
     * @return 实际存储实现
     */
    private IPushMessageStore resolve() {
        return storeProvider.stream().filter(store -> store != this).findFirst().orElse(fallback);
    }

    /**
     * 消息入源（透传实际存储）
     *
     * @param message 业务消息
     */
    @Override
    public void enqueue(PushMessage message) {
        resolve().enqueue(message);
    }

    /**
     * 原子取出一批待分发消息（透传实际存储）
     *
     * @param limit 单次最大取出条数
     * @return 待分发消息列表
     */
    @Override
    public List<PushMessage> fetchPending(int limit) {
        return resolve().fetchPending(limit);
    }

    /**
     * 标记消息已分发完成（透传实际存储）
     *
     * @param messageId 消息唯一标识
     */
    @Override
    public void markDispatched(String messageId) {
        resolve().markDispatched(messageId);
    }

    /**
     * 统计待分发消息数量（透传实际存储）
     *
     * @return 待分发消息数量
     */
    @Override
    public int pendingCount() {
        return resolve().pendingCount();
    }

}
