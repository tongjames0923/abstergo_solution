package com.tbs.common.util;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Spring工具类
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
    public static <T> T buildBeanFromProperties(Supplier<T> defaultBean, Class<? extends T> property,
        Function<Class<? extends T>, T> func) {
        T bean = func.apply(property);
        return bean != null ? bean : defaultBean.get();
    }

}
