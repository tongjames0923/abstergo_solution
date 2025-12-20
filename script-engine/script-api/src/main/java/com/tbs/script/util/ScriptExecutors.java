package com.tbs.script.util;

import com.tbs.script.api.context.IScriptContext;
import com.tbs.script.api.executor.ICompiledExecutor;
import com.tbs.script.model.ScriptResult;

import java.util.concurrent.CompletableFuture;

/**
 * 脚本执行工具类
 *
 * @author tboss
 */
public final class ScriptExecutors {

    private ScriptExecutors() {
        // 防止实例化
    }

    /**
     * 执行脚本并返回结果
     *
     * @param executor 脚本执行器
     * @param context  脚本上下文
     * @return 脚本执行结果
     */
    public static ScriptResult execute(ICompiledExecutor executor, IScriptContext context) {
        return executor.execute();
    }

    /**
     * 异步执行脚本并返回CompletableFuture
     *
     * @param executor 脚本执行器
     * @param context  脚本上下文
     * @return CompletableFuture包装的脚本执行结果
     */
    public static CompletableFuture<ScriptResult> executeAsync(ICompiledExecutor executor, IScriptContext context) {
        return executor.executeAsync();
    }
}