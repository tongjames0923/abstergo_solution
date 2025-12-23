package com.tbs.script.util;

import com.tbs.script.api.context.IScriptContext;
import com.tbs.script.api.context.impl.SimpleLocalMapSciptContext;

/**
 * 脚本上下文工具类
 *
 * @author tboss
 */
public final class ScriptContexts {

    /**
     * 创建简单的脚本上下文
     *
     * @return 脚本上下文实例
     */
    public static IScriptContext createSimpleContext() {
        return new SimpleLocalMapSciptContext();
    }

    /**
     * 基于父上下文创建子上下文
     *
     * @param parent 父上下文
     * @return 子脚本上下文实例
     */
    public static IScriptContext createChildContext(IScriptContext parent) {
        return parent.createChildScriptContext();
    }
}
