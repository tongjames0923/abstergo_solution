package com.tbs.script.exception;

/**
 * 脚本执行异常
 *
 * @author tboss
 */
public class ScriptExecutionException extends ScriptException {
    public ScriptExecutionException(String message) {
        super(message);
    }

    public ScriptExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}