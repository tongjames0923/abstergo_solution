package com.tbs.common.util.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    /**
     * 将集合均分成n个集合
     *
     * @param array
     * @param size
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> partition(Collection<T> array, int size) {
        if (isEmpty(array)) {
            return Collections.emptyList();
        }
        if (size <= 0) {
            throw new IllegalArgumentException("分割大小必须为正数");
        }

        List<T> list = new ArrayList<>(array);
        int partitionSize = (int)Math.ceil((double)list.size() / size);
        List<List<T>> result = new ArrayList<>(partitionSize);

        for (int i = 0; i < partitionSize; i++) {
            int start = i * size;
            int end = Math.min(start + size, list.size());
            result.add(list.subList(start, end));
        }
        return result;
    }

}
