package com.tbs.script.api.context.impl;

import com.tbs.script.api.compiler.IScriptCompilerMeta;
import com.tbs.script.api.context.IScriptContext;
import com.tbs.script.api.context.IScriptContextFactory;
import org.jetbrains.annotations.NotNull;

/**
 * 空的简单脚本上下文工厂
 *
 * @author tboss
 */
public class EmptySimpleScriptContextFactory implements IScriptContextFactory {
    @Override
    public IScriptContext createContextForCompiler(@NotNull IScriptCompilerMeta meta) {
        return new SimpleLocalMapSciptContext();
    }
}
