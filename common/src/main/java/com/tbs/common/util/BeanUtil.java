package com.tbs.common.util;

import com.tbs.common.exception.FormattedRuntimeException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Bean工具类
 *
 * @author abstergo
 */
public class BeanUtil extends cn.hutool.core.bean.BeanUtil {
    /**
     * 强制类型转换。
     *
     * @param <T>  泛型类型
     * @param <T1> 目标泛型类型
     * @param v    要转换的对象
     * @return 转换后的对象
     */
    public static <T, T1 extends T> T1 getAs(T v) {
        return (T1)v;
    }

    /**
     * 检查对象是否为指定基类或接口的实例。
     *
     * @param value 对象实例
     * @param base  基类或接口类型
     * @return 如果是返回对象实例，否则返回null
     */
    public static <T> T getIfBase(@NotNull Object value, @NotNull Class<T> base) {
        return isBaseFrom(value.getClass(), base) ? (T)value : null;
    }

    /**
     * 判断一个类是否是另一个类或接口的子类或实现类。
     *
     * @param target 目标类
     * @param base   基类或接口
     * @return 如果target是base的子类或实现类返回true，否则false
     */
    public static final boolean isBaseFrom(@NotNull Class<?> target, @NotNull Class<?> base) {
        return base.isAssignableFrom(target);
    }

    /**
     * 根据属性类构建bean
     *
     * @param defaultBean 默认bean
     * @param property    属性类
     * @param func        构建函数
     * @param <T>         bean类型
     * @return bean
     */
    public static <T> T buildBeanFromProperties(Supplier<? extends T> defaultBean, Class<? extends T> property,
        Function<Class<? extends T>, T> func) {
        if (func == null) {
            return defaultBean.get();
        }
        T bean = func.apply(property);
        return bean != null ? bean : defaultBean.get();
    }

    /**
     * 根据无参构造函数构建bean
     *
     * @param c
     * @param <T>
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <T> T buildByEmptyArgs(Class<? extends T> c) {
        try {
            return c.getConstructor(null).newInstance();
        } catch (NoSuchMethodException e) {
            throw new FormattedRuntimeException(e, "无参构造函数不存在");
        } catch (InstantiationException | IllegalAccessException e) {
            throw new FormattedRuntimeException(e, "构建bean失败");
        } catch (InvocationTargetException e) {
            throw new FormattedRuntimeException(e.getTargetException(), "构建bean失败");
        }
    }
}
