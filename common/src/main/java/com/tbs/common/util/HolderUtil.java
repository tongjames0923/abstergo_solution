package com.tbs.common.util;

import com.tbs.common.constant.HolderBeanNameConstant;
import com.tbs.common.holder.IContextHolder;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.holder.context.impl.LocalStoredContext;
import com.tbs.common.holder.context.impl.ThreadLocalStoredContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
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

    /**
     * 上下文持有者
     */
    @Lazy
    @Resource
    @Qualifier(HolderBeanNameConstant.MAIN_CONTEXT_HOLDER)
    private IContextHolder contextHolder;

    /**
     * 单例
     */
    private static HolderUtil instance;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        instance = this;
    }

    /**
     * 获取上下文持有者
     *
     * @return
     */
    public static IContextHolder getContextHolder() {
        return instance.contextHolder;
    }

    /**
     * 获取指定ID的上下文
     *
     * @param contextId
     * @return
     */
    public static IContext getContext(String contextId) {
        return getContextHolder().getContextByContextId(contextId);
    }

    /**
     * 获取默认的LocalStoredContext
     *
     * @return
     */
    public static IContext getDefaultLocalStoredContext() {
        return getContext(LocalStoredContext.LOCAL_STORED_CONTEXT_ID);
    }

    /**
     * 获取默认的ThreadLocalStoredContext
     *
     * @return
     */
    public static IContext getDeaultThreadLocalStoredContext() {
        return getContext(ThreadLocalStoredContext.THREAD_LOCAL_STORED_CONTEXT_ID);
    }

    /**
     * 根据类型生成类型值对应的key
     *
     * @param clazz
     * @return
     */
    private static String getClassValueKey(Class<?> clazz) {
        return "type:" + clazz.getName();
    }

    /**
     * 添加值
     *
     * @param context 上下文
     * @param key     键
     * @param value   值
     * @return this
     */
    public static HolderUtil put(IContext context, String key, Object value) {
        validContext(context);
        context.set(key, value);
        return instance;
    }

    public static IContext validContext(IContext context) {
        AssertUtil.notNull(context, "环境上下文不能为空");
        AssertUtil.isTrue(!context.isValid(), "环境上下文{0}已失效", context.getContextId());
        return context;
    }

    /**
     * 获取值
     *
     * @param context 上下文
     * @param key     键
     * @return 值
     */
    public static Object get(IContext context, String key) {
        validContext(context);
        return context.get(key);
    }

    /**
     * 添加值
     *
     * @param context 上下文
     * @param clazz   类型
     * @param value   值
     * @return this
     */
    public static <T> HolderUtil put(IContext context, Class<? extends T> clazz, T value) {
        validContext(context);
        context.set(getClassValueKey(clazz), value);
        return instance;
    }

    /**
     * 添加值,根据类型加入，如果类型已存在则覆盖
     *
     * @param context 上下文
     * @param value   值 不能为空
     * @return this
     */
    public static <T> HolderUtil put(IContext context, @NotNull T value) {
        validContext(context);
        return put(context, value.getClass(), value);
    }

    /**
     * 获取值
     *
     * @param context 上下文
     * @param clazz   类型
     * @return 值
     */
    public static <T> T get(IContext context, Class<? extends T> clazz) {
        validContext(context);
        return BeanUtil.getIfBase(context.get(getClassValueKey(clazz)), clazz);
    }

}
