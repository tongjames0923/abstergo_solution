package com.tbs.script.api.engine;

import com.tbs.script.api.compiler.IScriptCompiler;
import com.tbs.script.api.compiler.IScriptCompilerMeta;
import com.tbs.script.api.context.IScriptContextFactory;
import com.tbs.script.model.ScriptCompilerSpec;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
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
     * @param major        版本
     * @param min
     * @return 脚本编译器
     */
    Optional<ScriptCompilerSpec> getCompilerSpecWithVersion(String compilerName, Integer major, Integer min);

    /**
     * 获取所有脚本编译器
     *
     * @return 所有脚本编译器
     */
    Collection<ScriptCompilerSpec> getAllCompilerSpec();

    /**
     * 注册脚本编译器
     *
     * @param meta           脚本编译器元数据
     * @param compiler       脚本编译器
     * @param contextFactory 脚本上下文工厂
     */
    void registerCompilerSpec( IScriptCompilerMeta meta,  IScriptCompiler compiler,
        IScriptContextFactory contextFactory);

}
