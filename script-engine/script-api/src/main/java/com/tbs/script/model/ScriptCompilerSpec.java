package com.tbs.script.model;

import com.tbs.script.api.compiler.IScriptCompiler;
import com.tbs.script.api.compiler.IScriptCompilerMeta;
import com.tbs.script.api.context.IScriptContext;
import lombok.Getter;

/**
 * 脚本编译规范
 *
 * @author tboss
 */
@Getter
public class ScriptCompilerSpec {
    /**
     * 脚本编译器
     */
    private IScriptCompiler compiler;

    /**
     * 脚本编译器元数据
     */
    private IScriptCompilerMeta compilerMeta;

    /**
     * 脚本上下文
     */
    private IScriptContext scriptContext;

    public ScriptCompilerSpec setCompiler(IScriptCompiler compiler) {
        this.compiler = compiler;
        return this;
    }

    public ScriptCompilerSpec setScriptContext(IScriptContext scriptContext) {
        this.scriptContext = scriptContext;
        return this;
    }
}
