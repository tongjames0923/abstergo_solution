package com.tbs.common.util;

import com.tbs.common.constant.HolderBeanNameConstant;
import com.tbs.common.holder.IContextHolder;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.holder.context.impl.LocalStoredContext;
import com.tbs.common.holder.context.impl.ThreadLocalStoredContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 上下文工具类
 *
 * @author abstergo
 */
@Component
public final class HolderUtil {

    @Resource
    @Qualifier(HolderBeanNameConstant.MAIN_CONTEXT_HOLDER)
    private IContextHolder contextHolder;

    private static HolderUtil instance;

    @PostConstruct
    public void init() {
        instance = this;
    }

    public static IContextHolder getContextHolder() {
        return instance.contextHolder;
    }

    public static IContext getContext(String contextId) {
        return getContextHolder().getContextByContextId(contextId);
    }

    public static IContext getDefaultLocalStoredContext() {
        return getContext(LocalStoredContext.LOCAL_STORED_CONTEXT_ID);
    }

    public static IContext getDeaultThreadLocalStoredContext() {
        return getContext(ThreadLocalStoredContext.THREAD_LOCAL_STORED_CONTEXT_ID);
    }

    private static String getClassValueKey(Class<?> clazz) {
        return "type:" + clazz.getName();
    }

    public static HolderUtil put(IContext context, String key, Object value) {
        context.set(key, value);
        return instance;
    }

    public static Object get(IContext context, String key) {
        return context.get(key);
    }

    public static <T> HolderUtil put(IContext context, Class<? extends T> clazz, T value) {
        context.set(getClassValueKey(clazz), value);
        return instance;
    }

    public static <T> T get(IContext context, Class<? extends T> clazz) {
        return BeanUtil.getIfBase(context.get(getClassValueKey(clazz)), clazz);
    }

}
