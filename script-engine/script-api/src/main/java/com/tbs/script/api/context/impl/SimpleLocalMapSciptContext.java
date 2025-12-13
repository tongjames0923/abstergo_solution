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
 *
 * @author tboss
 */

@Slf4j
public class SimpleLocalMapSciptContext implements IScriptContext {

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

    private String contextLockKeyPrefix = "script_context_lock_";

    /**
     * 上下文ID
     */
    private String contextId;

    public SimpleLocalMapSciptContext() {
        valid = true;
        contextId = UuidUtil.getInstance().getUuidString();
    }

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

    @Override
    public void removeVariable(String name, ScriptScope scope) {
        checkValid();
        var map = getVariableMap(scope);
        if (map.containsKey(name)) {
            map.remove(name);
        }
    }

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

        } else {
            return null;
        }
    }

    @Override
    public IScriptContext createChildScriptContext() {
        checkValid();
        return this;
    }

    @Override
    public Collection<String> listLocalVariables() {
        checkValid();
        return localVariables.keySet();
    }

    @Override
    public Collection<String> listGlobalVariables() {
        checkValid();
        return globalVariables.keySet();
    }

    @Override
    public void destroy() {
        valid = false;
        contextId = null;
        localVariables.clear();
        globalVariables.clear();
    }

    @Override
    public void rebuild() {
        valid = true;
        contextId = UuidUtil.getInstance().getUuidString();
        globalVariables.clear();
        localVariables.clear();
    }

    @Override
    public boolean isValid() {
        return valid && StrUtil.isNotEmpty(contextId);
    }
}
