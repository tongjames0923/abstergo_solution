package com.tbs.common.util;

import java.util.Collection;

/**
 * 集合工具类
 *
 * @author abstergo
 */
public class CollUtil extends cn.hutool.core.collection.CollUtil {

    /**
     * 判断数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    public static <T> T first(T[] array) {
        if (isEmpty(array)) {
            return null;
        }
        return array[0];
    }

    public static <T> T last(T[] array) {
        if (isEmpty(array)) {
            return null;
        }
        return array[array.length - 1];
    }

    public static <T> T first(Collection<T> collection) {
        return isEmpty(collection) ? null : collection.iterator().next();
    }

}
