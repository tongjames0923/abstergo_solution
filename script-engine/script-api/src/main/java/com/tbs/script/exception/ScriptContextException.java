package com.tbs.script.exception;

/**
 * 脚本上下文异常
 *
 * @author tboss
 */
public class ScriptContextException extends ScriptException {
    public ScriptContextException(String message) {
        super(message);
    }

    public ScriptContextException(String message, Throwable cause) {
        super(message, cause);
    }
}