package com.tbs.script.api.executor;

import com.tbs.script.api.compiler.IScriptCompiler;
import com.tbs.script.api.context.IScriptContext;
import com.tbs.script.model.ScriptResult;

import java.util.concurrent.CompletableFuture;

/**
 * 脚本执行接口
 *
 * @author tboss
 */
public interface ICompiledExecutor {

    /**
     * 执行脚本
     *
     * @param context 脚本上下文
     * @return 脚本执行结果
     */
    ScriptResult execute();

    /**
     * 异步执行脚本
     *
     * @param context 脚本上下文
     * @return 脚本执行结果
     */
    default CompletableFuture<ScriptResult> executeAsync() {
        return CompletableFuture.supplyAsync(this::execute);
    }

    /**
     * 获取脚本上下文
     *
     * @return 脚本上下文
     */
    IScriptContext getContext();

    /**
     * 获取脚本编译器
     *
     * @return 脚本编译器
     */
    IScriptCompiler getCompiler();

    /**
     * 脚本是否正在执行
     *
     * @return 脚本是否正在执行
     */
    boolean isExecuting();

}
