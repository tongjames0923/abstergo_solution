package com.tbs.script.api.context;

import com.tbs.script.api.compiler.IScriptCompilerMeta;
import org.jetbrains.annotations.NotNull;

/**
 * 脚本上下文工厂接口
 *
 * @author tboss
 */
public interface IScriptContextFactory {

    /**
     * 创建脚本上下文
     *
     * @param meta 脚本编译器元数据
     * @return 脚本上下文
     */
    IScriptContext createContextForCompiler( IScriptCompilerMeta meta);
}
