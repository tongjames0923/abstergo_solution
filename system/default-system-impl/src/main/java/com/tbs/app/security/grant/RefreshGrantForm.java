package com.tbs.app.security.grant;

import java.io.Serializable;

/**
 * 刷新令牌授权表单
 * <p>
 * 刷新令牌本身存放于 {@code REFRESH_TOKEN} Cookie（HttpOnly），由 {@code IRefreshTokenService}
 * 从请求上下文读取，故刷新授权无需任何请求体字段，这里仅作为统一门面反序列化的占位表单。
 * </p>
 *
 * @author abstergo
 */
public class RefreshGrantForm implements Serializable {
}
