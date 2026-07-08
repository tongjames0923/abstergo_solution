package com.tbs.common.util.base;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 集合工具类
 *
 * @author abstergo
 */
public class CollUtil extends cn.hutool.core.collection.CollUtil {

    /**
     * 判断指定值是否在指定数组中
     *
     * @param val 要判断的值
     * @param arr 数组
     * @param <T> 数组元素类型
     * @return 如果指定值在数组中，则返回true，否则返回false
     */
    public static <T> boolean isIn(T val, T... arr) {
        if (isEmpty(arr)) {
            return false;
        }
        return Set.of(arr).contains(val);
    }

    /**
     * 判断指定值是否在指定集合中
     *
     * @param val 要判断的值
     * @param arr 集合
     * @param <T> 集合元素类型
     * @return 如果指定值在集合中，则返回true，否则返回false
     */
    public static <T> boolean isIn(T val, Collection<T> arr) {
        if (CollUtil.isEmpty(arr)) {
            return false;
        }
        return arr.contains(val);
    }

    /**
     * 判断对象数组是否为空
     *
     * @param arr 待检查的对象数组
     * @return 如果数组为null或长度为0，则返回true，否则返回false
     */
    public static boolean isEmpty(Object[] arr) {
        return arr == null || arr.length == 0;
    }

    /**
     * 判断对象数组是否不为空
     *
     * @param arr 待检查的对象数组
     * @return 如果数组不为null且长度大于0，则返回true，否则返回false
     */
    public static boolean isNotEmpty(Object[] arr) {
        return !isEmpty(arr);
    }

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
        return CollWrapper.of(iterable);
    }

    /**
     * 从数组创建集合包装器
     *
     * @param <T> 包装元素的类型
     * @param ts  源数组
     * @return 包含数组流的集合包装器实例
     */
    public static <T> CollWrapper<T> of(T... ts) {
        return CollWrapper.of(ts);
    }

    /**
     * 创建包含空流的集合包装器
     *
     * @param <T> 包装元素的类型
     * @return 包含空流的集合包装器实例
     */
    public static <T> CollWrapper<T> of() {
        return CollWrapper.empty();
    }

}
