package com.tbs.push.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.push.api.auth.IPushAuthenticator;
import com.tbs.push.api.auth.IPushCredentialStore;
import com.tbs.push.api.exception.PushProtocolException;
import com.tbs.push.constants.PushI18nConstant;
import com.tbs.push.model.PushAuthForm;
import com.tbs.push.model.PushAuthResult;
import com.tbs.push.model.PushCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 连接凭证服务
 * <p>
 * 统一连接建立流程的协议层核心，两传输（WebSocket/SSE）复用同一实例：
 * <ul>
 *     <li>签发 {@link #issue(PushAuthForm)}：认证器校验通过后预分配 sessionId，
 *     签发一次性 ticket（TTL 内有效）入存储，返回完整凭证供 HTTP 认证端点下发</li>
 *     <li>消费 {@link #consume(String)}：建连时原子取出即失效（一次性防重放），
 *     过期/缺失快速失败并抛协议异常（i18n）</li>
 * </ul>
 * ticket 日志脱敏（仅前缀），避免完整凭证泄露。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class PushCredentialService {

    private static final Logger log = LoggerFactory.getLogger(PushCredentialService.class);

    /**
     * 日志中凭证脱敏保留的前缀长度
     */
    private static final int TICKET_LOG_PREFIX_LENGTH = 8;

    /**
     * 默认凭证有效期（毫秒）
     */
    public static final long DEFAULT_TICKET_TTL_MILLIS = 60_000L;

    /**
     * 连接认证器
     */
    private final IPushAuthenticator authenticator;

    /**
     * 连接凭证存储
     */
    private final IPushCredentialStore credentialStore;

    /**
     * 凭证有效期（毫秒）
     */
    private final long ticketTtlMillis;

    /**
     * 构造连接凭证服务
     *
     * @param authenticator   连接认证器，不能为空
     * @param credentialStore 连接凭证存储，不能为空
     * @param ticketTtlMillis 凭证有效期（毫秒），非正数按默认TTL处理
     */
    public PushCredentialService(IPushAuthenticator authenticator, IPushCredentialStore credentialStore,
        long ticketTtlMillis) {
        AssertUtil.isTrue(authenticator == null).i18n(PushI18nConstant.AUTH_FORM_REQUIRED).throwIt();
        AssertUtil.isTrue(credentialStore == null).i18n(PushI18nConstant.CREDENTIAL_REQUIRED).throwIt();
        this.authenticator = authenticator;
        this.credentialStore = credentialStore;
        this.ticketTtlMillis = ticketTtlMillis > 0 ? ticketTtlMillis : DEFAULT_TICKET_TTL_MILLIS;
    }

    /**
     * 签发连接凭证
     * <p>
     * 认证失败抛协议异常（i18n，携带业务失败原因）；通过则预分配 sessionId 并 签发一次性 ticket 入存储，返回凭证（含过期时间）供认证端点下发。
     * </p>
     *
     * @param form 客户端提交的认证表单
     * @return 签发的连接凭证
     * @throws PushProtocolException 认证失败时抛出
     */
    public PushCredential issue(PushAuthForm form) {
        AssertUtil.isTrue(form == null).i18n(PushI18nConstant.AUTH_FORM_REQUIRED).throwIt();
        AssertUtil.isTrue(form.getToken() == null).i18n(PushI18nConstant.AUTH_TOKEN_REQUIRED).throwIt();
        PushAuthResult authResult = authenticator.authenticate(form);
        AssertUtil.isTrue(authResult == null).i18n(PushI18nConstant.AUTH_FAILED).throwIt();
        AssertUtil.isTrue(!authResult.isAuthenticated())
            .i18n(PushI18nConstant.AUTH_FAILED, authResult.getFailureReason()).throwIt();
        PushCredential credential = new PushCredential();
        credential.setTicket(UuidUtil.getInstance().getUuidString());
        credential.setSessionId(UuidUtil.getInstance().getUuidString());
        credential.setAttributes(authResult.getAttributes());
        credential.setExpireAt(new Date(System.currentTimeMillis() + ticketTtlMillis));
        credentialStore.save(credential);
        log.info("连接凭证已签发, sessionId:{}, ticket:{}...", credential.getSessionId(),
            maskTicket(credential.getTicket()));
        return credential;
    }

    /**
     * 原子消费连接凭证
     * <p>
     * 缺失/过期/已使用统一抛协议异常（i18n），由调用方决定关闭连接或返回错误响应。
     * </p>
     *
     * @param ticket 一次性连接凭证
     * @return 消费成功的连接凭证（含预分配 sessionId 与认证属性）
     * @throws PushProtocolException 凭证缺失、无效或已过期时抛出
     */
    public PushCredential consume(String ticket) {
        AssertUtil.isTrue(StrUtil.isEmpty(ticket)).i18n(PushI18nConstant.TICKET_REQUIRED).throwIt();
        PushCredential credential = credentialStore.consume(ticket);
        AssertUtil.isTrue(credential == null).i18n(PushI18nConstant.TICKET_INVALID, maskTicket(ticket)).throwIt();
        log.info("连接凭证消费成功, sessionId:{}, ticket:{}...", credential.getSessionId(),
            maskTicket(credential.getTicket()));
        return credential;
    }

    /**
     * 凭证日志脱敏（仅保留前缀）
     *
     * @param ticket 凭证原文
     * @return 脱敏后的凭证
     */
    private String maskTicket(String ticket) {
        if (StrUtil.isEmpty(ticket) || ticket.length() <= TICKET_LOG_PREFIX_LENGTH) {
            return "***";
        }
        return ticket.substring(0, TICKET_LOG_PREFIX_LENGTH);
    }

}
