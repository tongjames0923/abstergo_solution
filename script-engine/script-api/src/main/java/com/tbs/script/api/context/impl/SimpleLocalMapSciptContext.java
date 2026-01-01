package com.tbs.script.api.context.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.script.api.context.IScriptContext;
import com.tbs.script.api.context.ScriptScope;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 简单的脚本上下文实现
 * <p>
 * 该实现支持变量的设置、获取和删除操作，并支持父子上下文继承关系。
 * 子上下文可以访问父上下文中的变量，但对子上下文的修改不会影响父上下文。
 *
 * @author tboss
 */
@Slf4j
public class SimpleLocalMapSciptContext implements IScriptContext {

    /**
     * 变量内部封装类
     * <p>
     * 用于封装变量的名称、值、类型和作用域信息
     */
    @Data
    @AllArgsConstructor
    private static final class Variable {
        private final String name;
        private final Object value;
        private final Class<?> type;
        private final ScriptScope scope;

    }

    private boolean valid = true;
    private final Map<String, Variable> localVariables = new HashMap<>();
    private final Map<String, Variable> globalVariables = new HashMap<>();
    private final SimpleLocalMapSciptContext parentContext;

    private String contextLockKeyPrefix = "script_context_lock_";

    /**
     * 上下文ID
     */
    private String contextId;

    /**
     * 构造一个根脚本上下文（无父上下文）
     */
    public SimpleLocalMapSciptContext() {
        this(null); // 根上下文
    }

    /**
     * 构造一个脚本上下文
     *
     * @param parentContext 父上下文，可以为null表示根上下文
     */
    public SimpleLocalMapSciptContext(SimpleLocalMapSciptContext parentContext) {
        this.parentContext = parentContext;
        valid = true;
        contextId = UuidUtil.getInstance().getUuidString();
    }

    /**
     * 设置指定作用域下的变量
     *
     * @param name  变量名称，不能为空
     * @param value 变量值，可以为null
     * @param type  变量类型，用于类型转换和校验，不能为空
     * @param scope 变量作用域，决定变量可见范围，不能为空
     * @param <T>   泛型参数，与type参数对应
     */
    @Override
    public <T> void setVariable(String name, T value, Class<T> type, ScriptScope scope) {
        checkValid();
        var map = getVariableMap(scope);
        AssertUtil.hasTextWithLevel(name, ErrorLevelEnum.SYSTEM_ERROR, "变量名称不能为空");
        AssertUtil.isTrueSystemError(value == null && type == null).message("变量值和类型不能同时为空").throwIt();
        if (map.containsKey(name)) {
            log.debug("脚本上下文{} 中的 {} 变量{}已存在，将覆盖变量值", contextId, scope.getValue(), name);
        }
        map.put(name, new Variable(name, value, type, scope));
    }

    /**
     * 删除指定作用域下的变量
     *
     * @param name  变量名称，不能为空
     * @param scope 变量作用域，不能为空
     */
    @Override
    public void removeVariable(String name, ScriptScope scope) {
        checkValid();
        var map = getVariableMap(scope);
        if (map.containsKey(name)) {
            map.remove(name);
        }
    }

    /**
     * 根据作用域获取对应的变量映射表
     *
     * @param scope 作用域
     * @return 对应的变量映射表
     */
    private Map<String, Variable> getVariableMap(ScriptScope scope) {
        switch (scope) {
            case Local:
                return localVariables;
            case Global:
                return globalVariables;
            default:
                AssertUtil.throwError(() -> new IllegalArgumentException("未知的变量作用域"));
                return null;
        }
    }

    /**
     * 获取指定作用域下的变量值
     *
     * @param name  变量名称，不能为空
     * @param type  变量类型，用于类型转换，不能为空
     * @param scope 变量作用域，不能为空
     * @param <T>   泛型参数，与type参数对应
     * @return 返回Optional包装的变量值。如果变量存在则返回包含值的Optional(值可能为null)； 如果变量不存在但在父上下文中存在则返回父上下文中的值；
     *     如果变量在任何上下文中都不存在则返回空的Optional
     */
    @Override
    public <T> Optional<T> getVariable(String name, Class<T> type, ScriptScope scope) {
        checkValid();
        var map = getVariableMap(scope);
        if (map.containsKey(name)) {
            var val = map.get(name);
            if (val.getValue() == null) {
                return Optional.empty();
            } else {
                return Optional.of(type.cast(val.getValue()));
            }
        } else if (parentContext != null) {
            // 在父上下文中查找变量
            return parentContext.getVariable(name, type, scope);
        } else {
            return Optional.empty();
        }
    }

    /**
     * 创建子脚本上下文
     * <p>
     * 子脚本上下文继承父上下文的所有变量，并可访问父上下文中的变量， 但对子上下文的修改不影响父上下文
     *
     * @return 新创建的子脚本上下文实例，不为空
     */
    @Override
    public IScriptContext createChildScriptContext() {
        checkValid();
        return new SimpleLocalMapSciptContext(this);
    }

    /**
     * 列出所有局部变量名称
     *
     * @return 所有局部变量名称的集合，不为空
     */
    @Override
    public Collection<String> listLocalVariables() {
        checkValid();
        return localVariables.keySet();
    }

    /**
     * 列出所有全局变量名称
     *
     * @return 所有全局变量名称的集合，不为空
     */
    @Override
    public Collection<String> listGlobalVariables() {
        checkValid();
        return globalVariables.keySet();
    }

    /**
     * 销毁上下文
     * <p>
     * 将上下文标记为无效并清理所有变量
     */
    @Override
    public void destroy() {
        valid = false;
        contextId = null;
        localVariables.clear();
        globalVariables.clear();
    }

    /**
     * 重建上下文
     * <p>
     * 将上下文重置为有效状态并清空所有变量
     */
    @Override
    public void rebuild() {
        valid = true;
        contextId = UuidUtil.getInstance().getUuidString();
        globalVariables.clear();
        localVariables.clear();
    }

    /**
     * 检查上下文是否有效
     *
     * @return 如果上下文有效且ID不为空则返回true，否则返回false
     */
    @Override
    public boolean isValid() {
        return valid && StrUtil.isNotEmpty(contextId);
    }
}
