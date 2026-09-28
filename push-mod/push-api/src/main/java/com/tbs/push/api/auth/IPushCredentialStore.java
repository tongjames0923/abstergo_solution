package com.tbs.push.api.auth;

import com.tbs.push.model.auth.PushCredential;

/**
 * 连接凭证存储契约
 * <p>
 * 一次性连接凭证的暂存抽象，认证签发与建连消费之间的桥梁：
 * 实现可以是内存（默认兜底）、Redis（跨实例共享）等任意介质。
 * </p>
 * <p>
 * 多实例部署要求：{@link #consume(String)} 必须具备"取出即失效"的原子语义，
 * 同一凭证只能被消费一次（如 Redis GETDEL），防止凭证重放。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IPushCredentialStore {

    /**
     * 保存连接凭证
     *
     * @param credential 连接凭证（ticket 唯一键）
     */
    void save(PushCredential credential);

    /**
     * 原子消费连接凭证
     * <p>
     * 取出即失效：凭证被取走后不允许再次消费；凭证不存在/已过期时返回null，
     * 由调用方区分处理。
     * </p>
     *
     * @param ticket 一次性连接凭证
     * @return 对应的连接凭证，不存在或已过期时返回null
     */
    PushCredential consume(String ticket);

}
