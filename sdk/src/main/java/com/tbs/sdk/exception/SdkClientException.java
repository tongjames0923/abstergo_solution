package com.tbs.sdk.exception;

/**
 * 客户端异常
 * <p>
 * 纯客户端侧错误时抛出：网络不可达、连接/读取超时、请求构造失败、
 * 响应格式非法、下载失败、配置非法等。与服务端业务错误
 * （{@link SdkServerException}）区分，便于调用方分别处理重试与服务端排障。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SdkClientException extends SdkException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造客户端异常
     *
     * @param message 异常描述
     */
    public SdkClientException(String message) {
        super(message);
    }

    /**
     * 构造客户端异常
     *
     * @param message 异常描述
     * @param cause   根因
     */
    public SdkClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
