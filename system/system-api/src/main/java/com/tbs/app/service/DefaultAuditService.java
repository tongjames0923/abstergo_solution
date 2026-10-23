package com.tbs.app.service;

import com.tbs.shared.interfaces.service.AbstractAuditService;

/**
 * 默认审计服务
 * <p>
 * 审计主体统一由 {@link AuditUserResolver} 解析：登录态记录真实用户ID，非登录态记录系统主体，
 * 避免字段留空后无法区分「系统行为」与「来源未知」。
 * </p>
 *
 * @author abstergo
 */
public class DefaultAuditService<E, K> extends AbstractAuditService<E, K, Long> {
    @Override
    public Long getCurrentUserId() {
        return AuditUserResolver.resolveCurrentUserId();
    }
}
