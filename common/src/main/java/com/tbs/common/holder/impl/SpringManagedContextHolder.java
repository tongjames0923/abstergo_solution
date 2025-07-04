package com.tbs.common.holder.impl;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.holder.IContextHolder;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.util.AssertUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring管理上下文持有者，通过Spring Bean管理提供IContext
 *
 * @author abstergo
 */
public class SpringManagedContextHolder implements IContextHolder {

    /**
     * 上下文环境Map
     */
    @Resource
    Map<String, IContext> contextMap;

    Map<String, IContext> contextIdMap = new HashMap<>();

    @PostConstruct
    void initMap() {
        contextMap.entrySet().stream().forEach(entry -> {
            AssertUtil.isTrue(contextIdMap.containsKey(entry.getValue().getContextId()),
                "Spring管理上下文环境持有器初始化异常，存在相同上下文环境Id的上下文,id:{0}",
                entry.getValue().getContextId());
            contextIdMap.put(entry.getValue().getContextId(), entry.getValue());
        });
    }

    @Override
    public IContext getContextByContextId(String contextId) {
        return contextIdMap.get(contextId);
    }

    /**
     * 通过Spring Bean名称获取上下文
     *
     * @param beanName
     * @return
     */
    public IContext getContextByBeanName(String beanName) {
        return contextMap.get(beanName);
    }

}
