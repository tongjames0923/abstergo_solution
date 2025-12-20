package com.tbs.script.api.compiler;

import com.tbs.script.model.ScriptCompilerFeatures;

/**
 * 脚本编译器元数据接口
 *
 * @author tboss
 */
public interface IScriptCompilerMeta {

    /**
     * 获取编译器名称
     *
     * @return 返回编译器名称
     */
    String getCompilerName();

    /**
     * 获取脚本编译器主版本号
     *
     * @return 获取脚本编译器主版本号
     */
    Integer getMajorVersion();

    /**
     * 获取脚本编译器次版本号
     *
     * @return 获取脚本编译器次版本号
     */
    Integer getMinorVersion();

    /**
     * 获取脚本语言
     *
     * @return 获取脚本语言
     */
    String getScriptLanguage();

    /**
     * 获取脚本编译器特性
     *
     * @return 获取脚本编译器特性
     */
    ScriptCompilerFeatures getFeatures();

}
