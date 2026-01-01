package com.tbs.script.api.context;

import com.tbs.common.context.IContext;

import java.util.Collection;
import java.util.Optional;

/**
 * 脚本上下文接口
 * <p>
 * 定义了脚本执行环境中的变量操作方法，包括设置、获取和删除不同作用域的变量。 支持两种作用域：全局作用域({@link ScriptScope#Global})和局部作用域({@link ScriptScope#Local})。
 * 实现类应保证线程安全并支持父子上下文继承关系。
 *
 * @author tboss
 */
public interface IScriptContext extends IContext {

    /**
     * 设置指定作用域下的变量
     *
     * @param name  变量名称，不能为空
     * @param value 变量值，可以为null
     * @param type  变量类型，用于类型转换和校验，不能为空
     * @param scope 变量作用域，决定变量可见范围，不能为空
     * @param <T>   泛型参数，与type参数对应
     */
    <T> void setVariable(String name, T value, Class<T> type, ScriptScope scope);

    /**
     * 设置局部变量，作用域为{@link ScriptScope#Local}
     *
     * @param name  变量名称，不能为空
     * @param value 变量值，可以为null
     * @param type  变量类型，用于类型转换和校验，不能为空
     * @param <T>   泛型参数，与type参数对应
     */
    default <T> void setLocalVariable(String name, T value, Class<T> type) {
        setVariable(name, value, type, ScriptScope.Local);
    }

    /**
     * 设置全局变量，作用域为{@link ScriptScope#Global}
     *
     * @param name  变量名称，不能为空
     * @param value 变量值，可以为null
     * @param type  变量类型，用于类型转换和校验，不能为空
     * @param <T>   泛型参数，与type参数对应
     */
    default <T> void setGlobalVariable(String name, T value, Class<T> type) {
        setVariable(name, value, type, ScriptScope.Global);
    }

    /**
     * 删除指定作用域下的变量
     *
     * @param name  变量名称，不能为空
     * @param scope 变量作用域，不能为空
     */
    void removeVariable(String name, ScriptScope scope);

    /**
     * 删除局部变量，作用域为{@link ScriptScope#Local}
     *
     * @param name 变量名称，不能为空
     */
    default void removeLocalVariable(String name) {
        removeVariable(name, ScriptScope.Local);
    }

    /**
     * 删除全局变量，作用域为{@link ScriptScope#Global}
     *
     * @param name 变量名称，不能为空
     */
    default void removeGlobalVariable(String name) {
        removeVariable(name, ScriptScope.Global);
    }

    /**
     * 获取指定作用域下的变量值
     *
     * @param name  变量名称，不能为空
     * @param type  变量类型，用于类型转换，不能为空
     * @param scope 变量作用域，不能为空
     * @param <T>   泛型参数，与type参数对应
     * @return 返回Optional包装的变量值。如果变量存在则返回包含值的Optional(值可能为null)； 如果变量不存在则返回空的Optional
     */
    <T> Optional<T> getVariable(String name, Class<T> type, ScriptScope scope);

    /**
     * 获取局部变量，作用域为{@link ScriptScope#Local}
     *
     * @param name 变量名称，不能为空
     * @param type 变量类型，用于类型转换，不能为空
     * @param <T>  泛型参数，与type参数对应
     * @return 返回Optional包装的变量值。如果变量存在则返回包含值的Optional(值可能为null)； 如果变量不存在则返回空的Optional
     */
    default <T> Optional<T> getLocalVariable(String name, Class<T> type) {
        return getVariable(name, type, ScriptScope.Local);
    }

    /**
     * 获取全局变量，作用域为{@link ScriptScope#Global}
     *
     * @param name 变量名称，不能为空
     * @param type 变量类型，用于类型转换，不能为空
     * @param <T>  泛型参数，与type参数对应
     * @return 返回Optional包装的变量值。如果变量存在则返回包含值的Optional(值可能为null)； 如果变量不存在则返回空的Optional
     */
    default <T> Optional<T> getGlobalVariable(String name, Class<T> type) {
        return getVariable(name, type, ScriptScope.Global);
    }

    /**
     * 创建子脚本上下文
     * <p>
     * 子脚本上下文继承父上下文的所有变量，并可访问父上下文中的变量，但对子上下文的修改不影响父上下文
     *
     * @return 新创建的子脚本上下文实例，不为空
     */
    IScriptContext createChildScriptContext();

    /**
     * 列出所有局部变量名称
     *
     * @return 所有局部变量名称的集合，不为空
     */
    Collection<String> listLocalVariables();

    /**
     * 列出所有全局变量名称
     *
     * @return 所有全局变量名称的集合，不为空
     */
    Collection<String> listGlobalVariables();
}
