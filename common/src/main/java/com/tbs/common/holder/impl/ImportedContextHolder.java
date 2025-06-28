package com.tbs.common.holder.impl;

import com.tbs.common.holder.IContextHolder;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.util.CollUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 手动导入的context
 *
 * @author abstergo
 */
public class ImportedContextHolder implements IContextHolder {
    /**
     * 存储的context
     */
    private Map<String, IContext> contextMap = new HashMap<>();

    @Override
    public IContext getContextByContextId(String contextId) {
        return contextMap.get(contextId);
    }

    /**
     * 构造函数
     *
     * @param contextMap
     */
    public ImportedContextHolder(List<IContext> contextMap) {
        if (CollUtil.isNotEmpty(contextMap)) {
            for (IContext context : contextMap) {
                this.addContext(context.getContextId(), context);
            }
        }
    }

    /**
     * 构造函数
     */
    public ImportedContextHolder() {
        this(null);
    }

    /**
     * 添加context
     *
     * @param contextId
     * @param context
     */
    public void addContext(String contextId, IContext context) {
        contextMap.put(contextId, context);
    }

    /**
     * 删除context
     *
     * @param contextId
     */
    public void removeContext(String contextId) {
        contextMap.remove(contextId);
    }

    /**
     * 删除所有context
     */
    public void removeAll() {
        contextMap.clear();
    }
}
