package com.tbs.script.api.engine;

import com.tbs.script.api.compiler.IScriptCompiler;
import com.tbs.script.api.compiler.IScriptCompilerMeta;
import com.tbs.script.api.context.IScriptContextFactory;
import com.tbs.script.model.ScriptCompilerSpec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 脚本引擎接口
 *
 * @author tboss
 */
public interface IScriptEngine {

    /**
     * 获取脚本编译器
     *
     * @param compilerName 编译器名称
     * @param version      版本
     * @return 脚本编译器
     */
    Optional<ScriptCompilerSpec> getCompilerSpecWithVersion(String compilerName, String version);

    /**
     * 获取脚本编译器
     *
     * @param compilerName 脚本编译器名称
     * @return 脚本编译器
     */
    Optional<ScriptCompilerSpec> getCompilerSpecWithLatestVersion(String compilerName);






    /**
     * 注册脚本编译器
     *
     * @param compilerSpec       脚本编译器元数据
     * @param compiler           脚本编译器
     * @param contextFactory     脚本上下文工厂
     */
    void registerCompilerSpec(@NotNull IScriptCompilerMeta compilerSpec, @NotNull IScriptCompiler compiler,
        @NotNull IScriptContextFactory contextFactory);


}
