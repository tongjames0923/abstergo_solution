package com.tbs.script.model;

import com.tbs.script.api.compiler.IScriptCompiler;
import com.tbs.script.api.compiler.IScriptCompilerMeta;
import com.tbs.script.api.context.IScriptContextFactory;
import com.tbs.script.api.executor.ICompiledExecutor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 脚本编译规范
 * <p>
 * 不可变类，包含脚本编译器及其相关元数据和上下文工厂
 *
 * @author tboss
 */
@Getter
public class ScriptCompilerSpec {
    /**
     * 脚本编译器
     */
    private final IScriptCompiler compiler;

    /**
     * 脚本编译器元数据
     */
    private final IScriptCompilerMeta compilerMeta;

    /**
     * 脚本上下文工厂
     */
    private final IScriptContextFactory contextFactory;

    /**
     * 构造函数
     *
     * @param compiler       脚本编译器
     * @param compilerMeta   脚本编译器元数据
     * @param contextFactory 脚本上下文工厂
     */
    public ScriptCompilerSpec(@NotNull IScriptCompiler compiler, @NotNull IScriptCompilerMeta compilerMeta,
        @NotNull IScriptContextFactory contextFactory) {
        this.compiler = Objects.requireNonNull(compiler, "compiler cannot be null");
        this.compilerMeta = Objects.requireNonNull(compilerMeta, "compilerMeta cannot be null");
        this.contextFactory = Objects.requireNonNull(contextFactory, "contextFactory cannot be null");
    }

    /**
     * 编译脚本
     *
     * @param script 脚本
     * @return 脚本执行器
     */
    public ICompiledExecutor compile(String script) {
        return compiler.compile(script, contextFactory.createContextForCompiler(compilerMeta));
    }

}
