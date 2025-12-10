package com.tbs.cache.impl;

import com.tbs.common.util.helper.FeatureWrapperHelper;
import com.tbs.common.util.helper.IFeatureWrapperExtension;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.stereotype.Component;

/**
 * 事务感知缓存解包装扩展
 * <p>
 * 实现IFeatureWrapperExtension接口，提供对TransactionAwareCacheDecorator的解包装支持
 * 允许从事务感知缓存中获取原始缓存的特性
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Component
public class TransactionAwareCacheUnwrapperExtension implements IFeatureWrapperExtension {
    /**
     * 检查是否支持给定的实例
     * <p>
     * 如果实例是TransactionAwareCacheDecorator类型，则支持
     * </p>
     *
     * @param instance 要检查的实例
     * @param feature 要获取的特性类型
     * @param <T> 特性类型
     * @return 如果支持返回true，否则返回false
     */
    @Override
    public <T> boolean support(@NotNull Object instance, @NotNull Class<T> feature) {
        return instance instanceof TransactionAwareCacheDecorator;
    }

    /**
     * 从事务感知缓存中解包装获取特性
     * <p>
     * 将实例转换为TransactionAwareCacheDecorator，获取其目标缓存，然后使用FeatureWrapperHelper解包装
     * </p>
     *
     * @param instance 事务感知缓存实例
     * @param feature 要获取的特性类型
     * @param <T> 特性类型
     * @return 特性实例，如果不存在则返回null
     */
    @Override
    public <T> T unwrap(@NotNull Object instance, @NotNull Class<T> feature) {
        var cache = (TransactionAwareCacheDecorator)instance;
        return FeatureWrapperHelper.unwrap(cache.getTargetCache(), feature).orElse(null);
    }

    /**
     * 获取执行顺序
     * <p>
     * 返回0，表示默认顺序
     * </p>
     *
     * @return 执行顺序，值越小优先级越高
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
