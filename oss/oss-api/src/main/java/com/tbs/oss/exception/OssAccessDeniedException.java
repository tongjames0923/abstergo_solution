package com.tbs.oss.exception;

/**
 * 访问被拒绝异常（凭证无效、无权限等）
 */
public class OssAccessDeniedException extends OssException {

    public OssAccessDeniedException(String message) {
        super(message, null, null, null);
    }

    public OssAccessDeniedException(String message, Throwable cause) {
        super(message, null, null, cause);
    }
}
