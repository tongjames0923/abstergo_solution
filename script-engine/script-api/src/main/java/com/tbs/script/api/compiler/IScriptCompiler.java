package com.tbs.script.api.compiler;

import com.tbs.script.api.context.IScriptContext;
import com.tbs.script.api.executor.ICompiledExecutor;
import com.tbs.script.exception.ScriptException;

/**
 * 脚本编译接口
 *
 * @author tboss
 */
public interface IScriptCompiler {
    /**
     * 编译脚本
     *
     * @param script        脚本
     * @param scriptContext 脚本上下文
     * @return 返回脚本执行对象
     * @throws ScriptException 脚本编译异常
     */
    ICompiledExecutor compile(String script, IScriptContext scriptContext) throws ScriptException;

    /**
     * 获取编译器名称
     *
     * @return 返回编译器名称
     */
    String getCompilerName();

    /**
     * 获取编译器版本
     *
     * @return 获取编译器版本
     */
    String getCompilerVersion();

}
