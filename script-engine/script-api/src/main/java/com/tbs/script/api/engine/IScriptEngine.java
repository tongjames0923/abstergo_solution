package com.tbs.script.api.engine;

import com.tbs.script.api.compiler.IScriptCompiler;
import com.tbs.script.api.context.IScriptContext;

/**
 * 脚本引擎接口
 *
 * @author tboss
 */
public interface IScriptEngine {
    /**
     * 获取脚本编译器
     *
     * @return 脚本编译器
     */
    IScriptCompiler getCompiler();

    /**
     * 创建脚本上下文
     *
     * @return 脚本上下文
     */
    IScriptContext createScriptContext();

    /**
     * 获取脚本引擎支持的脚本语言
     *
     * @return 脚本语言
     */
    String scriptLanguage();



}
