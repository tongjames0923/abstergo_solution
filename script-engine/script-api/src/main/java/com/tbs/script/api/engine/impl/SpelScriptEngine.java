package com.tbs.script.api.engine.impl;

import com.tbs.script.api.compiler.impl.SpelCompiler;
import com.tbs.script.api.context.impl.EmptySimpleScriptContextFactory;
import com.tbs.script.api.engine.AbstractScriptEngine;

/**
 * SpEL脚本引擎
 *
 * @author abstergo
 */
public class SpelScriptEngine extends AbstractScriptEngine {

    @Override
    protected void importCompilerSpecs() {
        SpelCompiler compiler = new SpelCompiler();
        registerCompilerSpec(compiler, compiler, new EmptySimpleScriptContextFactory());
    }
}
