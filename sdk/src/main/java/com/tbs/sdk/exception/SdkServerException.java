package com.tbs.sdk.exception;

/**
 * 服务端异常
 * <p>
 * 服务端统一响应code为500或其他非认证类业务错误码时抛出，
 * 消息透传服务端i18n结果，业务码保留原始值供调用方分支处理。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SdkServerException extends SdkException {

    private static final long serialVersionUID = 1L;

    /**
     * 服务端统一响应业务码
     */
    private final int code;

    /**
     * 构造服务端异常
     *
     * @param code          服务端统一响应业务码
     * @param serverMessage 服务端i18n消息
     */
    public SdkServerException(int code, String serverMessage) {
        super(serverMessage);
        this.code = code;
    }

    /**
     * 获取服务端统一响应业务码
     *
     * @return 业务码
     */
    public int getCode() {
        return code;
    }
}
