package com.tbs.sdk.exception;

/**
 * 会话失效需重新登录异常
 * <p>
 * 服务端统一响应code为405（令牌失效需重登）或468（刷新令牌失败）时抛出，
 * 是 {@link SdkAuthException} 的语义细化子类：App/SDK 模式不实现 Cookie 刷新链路，
 * 收到本异常应引导用户重新调用登录接口换取令牌。调用方若需统一按认证失败处理，
 * 捕获父类 {@link SdkAuthException} 即可覆盖本异常。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SdkReloginRequiredException extends SdkAuthException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造重登异常
     *
     * @param code          服务端统一响应业务码（405或468）
     * @param serverMessage 服务端i18n消息
     */
    public SdkReloginRequiredException(int code, String serverMessage) {
        super(code, serverMessage);
    }
}
