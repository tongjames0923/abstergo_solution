package com.tbs.common.util;

import java.util.function.Consumer;

/**
 * Spring工具类
 *
 * @author abstergo
 */
public class SpringUtil extends cn.hutool.extra.spring.SpringUtil {

    /**
     * 获取bean，如果获取失败，则执行指定方法
     *
     * @param clazz
     * @param throwableConsumer
     * @param <T>
     * @return
     */
    public static <T> T getBeanSafe(Class<T> clazz, Consumer<Throwable> throwableConsumer) {
        try {
            return getBean(clazz);
        } catch (Throwable e) {
            throwableConsumer.accept(e);
        }
        return null;
    }

}
