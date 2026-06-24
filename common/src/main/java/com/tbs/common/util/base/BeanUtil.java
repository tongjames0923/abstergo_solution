package com.tbs.common.util.base;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Bean工具类
 * <p>
 * 提供Bean相关的工具方法，继承自hutool的BeanUtil类，
 * 并扩展了一些自定义的Bean操作功能，如强制类型转换、类型检查等
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class BeanUtil extends cn.hutool.core.bean.BeanUtil {
    /**
     * 强制类型转换
     * <p>
     * 将对象强制转换为指定的泛型类型
     * </p>
     *
     * @param <T>  原始泛型类型
     * @param <T1> 目标泛型类型
     * @param v    要转换的对象
     * @return 转换后的对象
     */
    public static <T, T1 extends T> T1 getAs(T v) {
        return (T1)v;
    }

    /**
     * 检查对象是否为指定基类或接口的实例
     * <p>
     * 如果对象是指定基类或接口的实例，则返回转换后的对象，否则返回null
     * </p>
     *
     * @param <T>   目标类型
     * @param value 对象实例
     * @param base  基类或接口类型
     * @return 如果是指定类型的实例则返回转换后的对象，否则返回null
     */
    public static <T> T getIfBase(@NotNull Object value, @NotNull Class<T> base) {
        return isBaseFrom(value.getClass(), base) ? (T)value : null;
    }

    /**
     * 判断一个类是否是另一个类或接口的子类或实现类
     * <p>
     * 使用Class.isAssignableFrom()方法判断类型关系
     * </p>
     *
     * @param target 目标类
     * @param base   基类或接口
     * @return 如果target是base的子类或实现类返回true，否则返回false
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
        if (func == null || property == null) {
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
            AssertUtil.throwError(() -> new FormattedRuntimeException(e, "没有无参构造函数"));
        } catch (InstantiationException | IllegalAccessException e) {
            AssertUtil.throwError(() -> new FormattedRuntimeException(e, "构建bean失败"));
        } catch (InvocationTargetException e) {
            AssertUtil.throwError(() -> new FormattedRuntimeException(e, "构建bean失败"));
        }
        return null;
    }
}
