package com.tbs.common.util;

import com.tbs.common.constant.HolderBeanNameConstant;
import com.tbs.common.function.SConsumer;
import com.tbs.common.holder.IContextHolder;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.holder.context.IContextDataSource;
import com.tbs.common.holder.context.impl.LocalStoredMapImplContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;

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
    public static <T> IContext<T> getContext(String contextId) {
        return getContextHolder().getContextByContextId(contextId);
    }

    /**
     * 获取默认的LocalStoredContext
     *
     * @return
     */
    public static <T> IContext<T> getDefaultLocalStoredContext() {
        return getContext(LocalStoredMapImplContext.LOCAL_STORED_CONTEXT_ID);
    }

    /**
     * 操作数据源
     *
     * @param context
     * @param consumer
     */
    public static <V> void operateDataSource(IContext<V> context, SConsumer<IContextDataSource> consumer) {
        if (context == null) {
            return;
        }
        if (BeanUtil.isBaseFrom(context.getClass(), IContextDataSource.class)) {
            consumer.accept((IContextDataSource)context);
        }
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
    public static <T> HolderUtil put(IContext<T> context, String key, T value) {
        validContext(context);
        operateDataSource(context, dataSource -> dataSource.set(key, value));
        return instance;
    }

    /**
     * 删除值
     *
     * @param context 上下文
     * @param key     键
     * @return this
     */
    public static HolderUtil remove(IContext context, String key) {
        validContext(context);
        operateDataSource(context, dataSource -> dataSource.remove(key));
        return instance;
    }

    /**
     * 验证上下文是否有效
     *
     * @param context
     * @param <T>
     * @return
     */
    public static <T> IContext validContext(IContext<T> context) {
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
    public static <T> T get(IContext<T> context, String key) {
        validContext(context);
        Object[] result = new Object[1];
        operateDataSource(context, dataSource -> result[0] = dataSource.get(key));
        return (T)result[0];
    }

    /**
     * 添加值
     *
     * @param context 上下文
     * @param clazz   类型
     * @param value   值
     * @return this
     */
    public static <T> HolderUtil putByClass(IContext<T> context, Class<?> clazz, T value) {
        validContext(context);
        put(context, getClassValueKey(clazz), value);
        return instance;
    }

    /**
     * 添加值,根据类型加入，如果类型已存在则覆盖
     *
     * @param context 上下文
     * @param value   值 不能为空
     * @return this
     */
    public static <T> HolderUtil put(IContext<T> context, @NotNull T value) {
        validContext(context);
        return putByClass(context, value.getClass(), value);
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
        Object result = get(context, getClassValueKey(clazz));
        return BeanUtil.getIfBase(result, clazz);
    }

    public static <T> Collection<T> values(IContext<T> context) {
        validContext(context);
        Collection<T> result = new java.util.ArrayList<>();
        operateDataSource(context, dataSource -> result.addAll(dataSource.values()));
        return result;
    }

}
