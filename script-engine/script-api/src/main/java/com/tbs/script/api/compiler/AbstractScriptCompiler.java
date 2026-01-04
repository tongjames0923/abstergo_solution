package com.tbs.script.api.compiler;

import com.tbs.script.api.context.IScriptContext;
import com.tbs.script.api.executor.ICompiledExecutor;
import com.tbs.script.api.executor.impl.SimpleScriptRunnerExecutor;
import com.tbs.script.exception.ScriptException;
import com.tbs.script.model.ScriptCompilerFeatures;
import com.tbs.script.model.ScriptResult;

/**
 * 抽象脚本编译器
 *
 * @author tongj
 */
public abstract class AbstractScriptCompiler implements IScriptCompiler, IScriptCompilerMeta {

    private ScriptCompilerFeatures features = new ScriptCompilerFeatures();

    public static interface IScriptRunner {
        /**
         * 执行脚本
         *
         * @param scriptContext 脚本上下文
         * @return 脚本执行结果
         */
        ScriptResult execute(IScriptContext scriptContext);
    }

    /**
     * 获取脚本执行器
     *
     * @param spec     脚本编译规范
     * @param script   脚本
     * @param features 脚本编译器特性
     * @return 脚本执行器
     */
    protected abstract IScriptRunner getScriptRunner(String script, ScriptCompilerFeatures features);

    /**
     * 编译脚本
     *
     * @param script        脚本
     * @param scriptContext 脚本上下文
     * @return 脚本执行对象
     * @throws ScriptException 脚本编译异常
     */
    @Override
    public ICompiledExecutor compile(String script, IScriptContext scriptContext) throws ScriptException {
        return new SimpleScriptRunnerExecutor(scriptContext, getScriptRunner(script, getFeatures()), this);
    }

    @Override
    public ScriptCompilerFeatures getFeatures() {
        return features;
    }
}
