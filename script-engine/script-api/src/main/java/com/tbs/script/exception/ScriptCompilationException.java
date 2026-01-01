package com.tbs.script.exception;

/**
 * 脚本编译异常
 *
 * @author tboss
 */
public class ScriptCompilationException extends ScriptException {
    public ScriptCompilationException(String message) {
        super(message);
    }

    public ScriptCompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}