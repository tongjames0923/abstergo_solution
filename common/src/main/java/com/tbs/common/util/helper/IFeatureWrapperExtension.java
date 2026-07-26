package com.tbs.common.util.helper;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;

/**
 * 包装器扩展接口
 */
public interface IFeatureWrapperExtension extends Ordered {

    /**
     * 是否支持
     *
     * @param instance
     * @return
     */
    <T> boolean support( Object instance,  Class<T> feature);

    /**
     * 解包
     *
     * @param instance
     * @param feature
     * @param <T>
     * @return
     */
    <T> T unwrap( Object instance,  Class<T> feature);

}
