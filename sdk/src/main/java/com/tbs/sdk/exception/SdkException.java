package com.tbs.sdk.exception;

/**
 * SDK异常基类
 * <p>
 * 所有SDK抛出的可预期异常的根类型。面向调用方的业务文案一律透传服务端
 * AjaxResult中的i18n消息（message字段），SDK自身不产出面向用户的文案。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SdkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造SDK异常
     *
     * @param message 异常描述（服务端消息透传或客户端链路描述）
     */
    public SdkException(String message) {
        super(message);
    }

    /**
     * 构造SDK异常
     *
     * @param message 异常描述
     * @param cause   根因
     */
    public SdkException(String message, Throwable cause) {
        super(message, cause);
    }
}
