package com.tbs.sdk.exception;

/**
 * 认证异常
 * <p>
 * 服务端统一响应code为401（未授权）或405（需要重新登录）时抛出，
 * 携带服务端i18n消息。调用方捕获本异常后应引导用户重新登录
 * （SDK不实现刷新令牌逻辑，后端刷新机制仅适用于web端）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SdkAuthException extends SdkException {

    private static final long serialVersionUID = 1L;

    /**
     * 服务端统一响应业务码（401或405）
     */
    private final int code;

    /**
     * 构造认证异常
     *
     * @param code          服务端统一响应业务码
     * @param serverMessage 服务端i18n消息
     */
    public SdkAuthException(int code, String serverMessage) {
        super(serverMessage);
        this.code = code;
    }

    /**
     * 获取服务端统一响应业务码
     *
     * @return 业务码（401或405）
     */
    public int getCode() {
        return code;
    }
}
