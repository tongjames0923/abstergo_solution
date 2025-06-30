package com.tbs.common.util;

import com.tbs.common.exception.FormattedRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Spring工具类
 * @author abstergo
 */
public class SpringUtil extends cn.hutool.extra.spring.SpringUtil {

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
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new FormattedRuntimeException(e, "构建bean失败");
        }
    }

}
