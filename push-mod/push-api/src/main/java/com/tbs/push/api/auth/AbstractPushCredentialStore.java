package com.tbs.push.api.auth;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.push.constants.PushI18nConstant;
import com.tbs.push.model.auth.PushCredential;

/**
 * 连接凭证存储基类
 * <p>
 * 以模板方法封装保存与消费的通用校验，具体介质实现只需覆盖do钩子方法：
 * <ul>
 *     <li>{@link #save(PushCredential)}前校验凭证完整性（ticket/sessionId/过期时间必填）</li>
 *     <li>{@link #consume(String)}对空ticket静默返回null</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public abstract class AbstractPushCredentialStore implements IPushCredentialStore {

    /**
     * 保存连接凭证（含完整性校验）
     *
     * @param credential 连接凭证
     */
    @Override
    public final void save(PushCredential credential) {
        AssertUtil.isTrue(credential == null).i18n(PushI18nConstant.CREDENTIAL_REQUIRED).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(credential.getTicket()))
            .i18n(PushI18nConstant.TICKET_REQUIRED).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(credential.getSessionId()))
            .i18n(PushI18nConstant.SESSION_ID_REQUIRED).throwIt();
        AssertUtil.isTrue(credential.getExpireAt() == null)
            .i18n(PushI18nConstant.CREDENTIAL_EXPIRE_REQUIRED).throwIt();
        doSave(credential);
    }

    /**
     * 原子消费连接凭证（空ticket静默返回null）
     *
     * @param ticket 一次性连接凭证
     * @return 对应的连接凭证，不存在或已过期时返回null
     */
    @Override
    public final PushCredential consume(String ticket) {
        if (StrUtil.isEmpty(ticket)) {
            return null;
        }
        return doConsume(ticket);
    }

    /**
     * 实际的保存动作
     *
     * @param credential 校验通过的连接凭证
     */
    protected abstract void doSave(PushCredential credential);

    /**
     * 实际的原子消费动作
     * <p>
     * 实现必须保证"取出即失效"的原子语义。
     * </p>
     *
     * @param ticket 一次性连接凭证
     * @return 对应的连接凭证，不存在或已过期时返回null
     */
    protected abstract PushCredential doConsume(String ticket);

}
