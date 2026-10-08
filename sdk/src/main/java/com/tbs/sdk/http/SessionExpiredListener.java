package com.tbs.sdk.http;

/**
 * 会话失效监听器
 * <p>
 * HTTP层收到 405（令牌失效）/ 468（刷新失败）业务码时，在抛出
 * {@link com.tbs.sdk.exception.SdkReloginRequiredException} 之前先行回调本监听器，
 * 宿主据此引导用户重新登录。App/SDK 模式不实现令牌刷新，收到失效即需重登。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@FunctionalInterface
public interface SessionExpiredListener {

    /**
     * 会话失效回调
     *
     * @param code    触发失效的业务码（405 或 468）
     * @param message 服务端i18n失效描述
     */
    void onSessionExpired(int code, String message);
}
