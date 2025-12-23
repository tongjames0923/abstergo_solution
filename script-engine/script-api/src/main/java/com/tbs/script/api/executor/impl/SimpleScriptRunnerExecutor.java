package com.tbs.script.api.executor.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.script.api.compiler.AbstractScriptCompiler;
import com.tbs.script.api.compiler.IScriptCompiler;
import com.tbs.script.api.context.IScriptContext;
import com.tbs.script.api.executor.ICompiledExecutor;
import com.tbs.script.model.ScriptResult;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 抽象编译后的脚本执行器
 *
 * @author tongj
 */
@Slf4j
public class SimpleScriptRunnerExecutor implements ICompiledExecutor {

    private IScriptContext context;

    private AbstractScriptCompiler.IScriptRunner scriptRunner;

    private IScriptCompiler compiler;

    private boolean isInitialized = false;
    private final AtomicBoolean isExecuting = new AtomicBoolean(false);

    public SimpleScriptRunnerExecutor(IScriptContext context, AbstractScriptCompiler.IScriptRunner scriptRunner,
        IScriptCompiler compiler) {
        this.context = context;
        this.scriptRunner = scriptRunner;
        this.compiler = compiler;
        this.isInitialized = true;
    }

    protected void resetContext() {
        if (!isInitialized) {
            log.warn("Executor not initialized");
            return;
        }
        if (context.isValid()) {
            context.destroy();
        }
        this.context.rebuild();
        log.info("Context reset");
    }

    @Override
    public ScriptResult execute() {
        AssertUtil.isTrue(!isInitialized).message("Executor not initialized").errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
            .throwIt();
        AssertUtil.isTrue(isExecuting()).message("Executor is executing").errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
            .throwIt();
        isExecuting.set(true);
        try {
            return scriptRunner.execute(context);
        } catch (Throwable e) {
            log.error("Executor execute error", e);
            return new ScriptResult(false, null, null, e.getMessage());
        } finally {
            isExecuting.set(false);
            resetContext();
        }

    }

    @Override
    public CompletableFuture<ScriptResult> executeAsync() {
        return CompletableFuture.supplyAsync(this::execute);
    }

    @Override
    public IScriptContext getContext() {
        return this.context;
    }

    @Override
    public IScriptCompiler getCompiler() {
        return this.compiler;
    }

    @Override
    public boolean isExecuting() {
        return this.isExecuting.get();
    }
}
