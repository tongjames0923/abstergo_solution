package com.tbs.updater.exception;

/**
 * 更新流程统一异常
 * <p>
 * 类库内所有可预期错误（配置、网络、服务端、解析、下载、脚本执行）
 * 统一包装为本异常抛出，调用方通过{@link ErrorType}区分错误类别。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class UpdateException extends Exception {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 错误类型枚举
     * <p>
     * CONFIG_ERROR-配置错误；NETWORK_ERROR-网络错误；SERVER_ERROR-服务端错误；
     * PARSE_ERROR-响应解析错误；DOWNLOAD_ERROR-下载错误；SCRIPT_ERROR-脚本执行错误。
     * </p>
     *
     * @author abstergo
     * @since 1.0.0
     */
    public enum ErrorType {
        CONFIG_ERROR,
        NETWORK_ERROR,
        SERVER_ERROR,
        PARSE_ERROR,
        DOWNLOAD_ERROR,
        SCRIPT_ERROR
    }

    /**
     * 错误类型
     */
    private final ErrorType errorType;

    /**
     * 构造更新异常
     *
     * @param errorType 错误类型
     * @param message   错误消息
     */
    public UpdateException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    /**
     * 构造更新异常并携带原始异常
     *
     * @param errorType 错误类型
     * @param message   错误消息
     * @param cause     原始异常
     */
    public UpdateException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    /**
     * 获取错误类型
     *
     * @return 错误类型枚举
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * 输出带错误类型的异常描述
     *
     * @return 异常描述字符串
     */
    @Override
    public String toString() {
        return "UpdateException[" + errorType + "] " + getMessage();
    }
}
