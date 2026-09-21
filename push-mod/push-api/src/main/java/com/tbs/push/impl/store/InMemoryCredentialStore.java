package com.tbs.push.impl.store;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.push.api.auth.AbstractPushCredentialStore;
import com.tbs.push.constants.PushI18nConstant;
import com.tbs.push.model.auth.PushCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存连接凭证存储（默认兜底实现）
 * <p>
 * 基于并发Map的凭证暂存实现：
 * <ul>
 *     <li>保存：容量满时拒绝新凭证并告警（凭证签发端会感知失败），防止慢客户端堆积拖垮内存</li>
 *     <li>消费：Map.remove 原子取出即失效，防凭证重放；取出时校验过期（惰性清理过期凭证）</li>
 * </ul>
 * 多实例部署需共享凭证时请注册持久化实现（如Redis）覆盖本兜底。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class InMemoryCredentialStore extends AbstractPushCredentialStore {

    /**
     * 默认容量上限
     */
    public static final int DEFAULT_CAPACITY = 10_000;
    private static final Logger log = LoggerFactory.getLogger(InMemoryCredentialStore.class);
    /**
     * 凭证表
     */
    private final Map<String, PushCredential> credentials = new ConcurrentHashMap<>();

    /**
     * 容量上限
     */
    private final int capacity;

    /**
     * 构造内存凭证存储（默认容量）
     */
    public InMemoryCredentialStore() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * 构造内存凭证存储
     *
     * @param capacity 容量上限，非正数按默认容量处理
     */
    public InMemoryCredentialStore(int capacity) {
        this.capacity = capacity > 0 ? capacity : DEFAULT_CAPACITY;
    }

    /**
     * 实际的保存动作（容量满时先清理过期凭证，仍满则断言拒绝）
     *
     * @param credential 校验通过的连接凭证
     */
    @Override
    protected void doSave(PushCredential credential) {
        if (credentials.size() >= capacity) {
            evictExpired(new Date());
            AssertUtil.isTrue(credentials.size() >= capacity).i18n(PushI18nConstant.CREDENTIAL_STORE_FULL)
                .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        }
        credentials.put(credential.getTicket(), credential);
    }

    /**
     * 实际的原子消费动作（remove取出即失效，过期凭证惰性清理）
     *
     * @param ticket 一次性连接凭证
     * @return 对应的连接凭证，不存在或已过期时返回null
     */
    @Override
    protected PushCredential doConsume(String ticket) {
        PushCredential credential = credentials.remove(ticket);
        if (credential == null) {
            return null;
        }
        if (credential.getExpireAt().before(new Date())) {
            return null;
        }
        return credential;
    }

    /**
     * 清理全部已过期凭证（容量压力下的辅助回收）
     *
     * @param now 当前时间
     */
    private void evictExpired(Date now) {
        boolean removed = credentials.values().removeIf(credential -> credential.getExpireAt().before(now));
        if (removed) {
            log.debug("内存凭证存储已清理过期凭证, 剩余:{}", credentials.size());
        }
    }

}
