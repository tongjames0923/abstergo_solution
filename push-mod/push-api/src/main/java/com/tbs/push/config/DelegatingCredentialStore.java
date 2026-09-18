package com.tbs.push.config;

import com.tbs.push.api.auth.IPushCredentialStore;
import com.tbs.push.model.PushCredential;
import org.springframework.beans.factory.ObjectProvider;

/**
 * 连接凭证存储懒委托包装
 * <p>
 * 装配阶段容器可能尚未引入真实凭证存储实现（如redis-mod提供的Redis实现晚于本配置解析），
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
class DelegatingCredentialStore implements IPushCredentialStore {

    /**
     * 凭证存储实现提供者
     */
    private final ObjectProvider<IPushCredentialStore> storeProvider;

    /**
     * 内存兜底实现
     */
    private final IPushCredentialStore fallback;

    /**
     * 构造懒委托包装
     *
     * @param storeProvider 凭证存储实现提供者
     * @param fallback      内存兜底实现
     */
    DelegatingCredentialStore(ObjectProvider<IPushCredentialStore> storeProvider, IPushCredentialStore fallback) {
        this.storeProvider = storeProvider;
        this.fallback = fallback;
    }

    /**
     * 解析实际生效的凭证存储实现
     *
     * @return 实际凭证存储实现
     */
    private IPushCredentialStore resolve() {
        return storeProvider.stream().filter(store -> store != this).findFirst().orElse(fallback);
    }

    /**
     * 保存连接凭证（透传实际存储）
     *
     * @param credential 连接凭证
     */
    @Override
    public void save(PushCredential credential) {
        resolve().save(credential);
    }

    /**
     * 原子消费连接凭证（透传实际存储）
     *
     * @param ticket 一次性连接凭证
     * @return 对应的连接凭证，不存在或已过期时返回null
     */
    @Override
    public PushCredential consume(String ticket) {
        return resolve().consume(ticket);
    }

}
