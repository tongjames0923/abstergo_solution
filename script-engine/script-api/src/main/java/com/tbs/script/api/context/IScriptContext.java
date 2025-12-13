package com.tbs.script.api.context;

import com.tbs.common.context.IContext;

import java.util.Collection;
import java.util.Optional;

/**
 * 脚本上下文接口
 * <p>
 * 定义了脚本执行环境中的变量操作方法，包括设置、获取和删除不同作用域的变量
 *
 * @author tboss
 */
public interface IScriptContext extends IContext {

    /**
     * 设置变量
     *
     * @param name  变量名称
     * @param value 变量值
     * @param type  变量类型
     * @param scope 变量作用域
     * @param <T>   泛型参数
     */
    <T> void setVariable(String name, T value, Class<T> type, ScriptScope scope);

    /**
     * 设置局部变量
     *
     * @param name  变量名称
     * @param value 变量值
     * @param type  变量类型
     * @param <T>   泛型参数
     */
    default <T> void setLocalVariable(String name, T value, Class<T> type) {
        setVariable(name, value, type, ScriptScope.Local);
    }

    /**
     * 设置全局变量
     *
     * @param name  变量名称
     * @param value 变量值
     * @param type  变量类型
     * @param <T>   泛型参数
     */
    default <T> void setGlobalVariable(String name, T value, Class<T> type) {
        setVariable(name, value, type, ScriptScope.Global);
    }

    /**
     * 删除变量
     *
     * @param name  变量名称
     * @param scope 变量作用域
     */
    void removeVariable(String name, ScriptScope scope);

    /**
     * 删除局部变量
     *
     * @param name 变量名称
     */
    default void removeLocalVariable(String name) {
        removeVariable(name, ScriptScope.Local);
    }

    /**
     * 删除全局变量
     *
     * @param name 变量名称
     */
    default void removeGlobalVariable(String name) {
        removeVariable(name, ScriptScope.Global);
    }

    /**
     * 获取变量
     *
     * @param name  变量名称
     * @param type  变量类型
     * @param scope 变量作用域
     * @param <T>   泛型参数
     * @return 返回指定类型和作用域的变量值，如果不存在则返回空Optional
     */
    <T> Optional<T> getVariable(String name, Class<T> type, ScriptScope scope);

    /**
     * 获取局部变量
     *
     * @param name 变量名称
     * @param type 变量类型
     * @param <T>  泛型参数
     * @return 返回指定类型的局部变量值，如果不存在则返回空Optional
     */
    default <T> Optional<T> getLocalVariable(String name, Class<T> type) {
        return getVariable(name, type, ScriptScope.Local);
    }

    /**
     * 获取全局变量
     *
     * @param name 变量名称
     * @param type 变量类型
     * @param <T>  泛型参数
     * @return 返回指定类型的全局变量值，如果不存在则返回空Optional
     */
    default <T> Optional<T> getGlobalVariable(String name, Class<T> type) {
        return getVariable(name, type, ScriptScope.Global);
    }

    /**
     * 创建子脚本上下文
     *
     * @return 返回子脚本上下文
     */
    IScriptContext createChildScriptContext();

    /**
     * 列出所有局部变量
     *
     * @return 返回所有局部变量名称的集合
     */
    Collection<String> listLocalVariables();

    /**
     * 列出所有全局变量
     *
     * @return 返回所有全局变量名称的集合
     */
    Collection<String> listGlobalVariables();
}
