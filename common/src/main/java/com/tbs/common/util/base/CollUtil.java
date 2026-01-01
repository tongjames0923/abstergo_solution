package com.tbs.common.util.base;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 集合工具类
 *
 * @author abstergo
 */
public class CollUtil extends cn.hutool.core.collection.CollUtil {

    /**
     * 创建包含指定流的集合包装器
     *
     * @param <T>    包装元素的类型
     * @param stream 要包装的流
     * @return 包含指定流的集合包装器实例
     */
    public static <T> CollWrapper<T> of(Stream<T> stream) {
        return new CollWrapper<>(stream);
    }

    /**
     * 从集合创建集合包装器
     *
     * @param <T>        包装元素的类型
     * @param collection 源集合
     * @return 包含集合流的集合包装器实例
     */
    public static <T> CollWrapper<T> of(Collection<T> collection) {
        return new CollWrapper<>(collection.stream());
    }

    /**
     * 从可迭代对象创建集合包装器
     *
     * @param <T>      包装元素的类型
     * @param iterable 源可迭代对象
     * @return 包含可迭代对象流的集合包装器实例
     */
    public static <T> CollWrapper<T> of(Iterable<T> iterable) {
        return new CollWrapper<>(StreamSupport.stream(iterable.spliterator(), false));
    }

    /**
     * 从数组创建集合包装器
     *
     * @param <T> 包装元素的类型
     * @param ts  源数组
     * @return 包含数组流的集合包装器实例
     */
    public static <T> CollWrapper<T> of(T... ts) {
        return new CollWrapper<>(Arrays.stream(ts));
    }

    /**
     * 创建包含空流的集合包装器
     *
     * @param <T> 包装元素的类型
     * @return 包含空流的集合包装器实例
     */
    public static <T> CollWrapper<T> of() {
        return new CollWrapper<>(Stream.empty());
    }

}
