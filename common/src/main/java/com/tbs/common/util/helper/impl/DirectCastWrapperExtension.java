package com.tbs.common.util.helper.impl;

import com.tbs.common.util.helper.IFeatureWrapperExtension;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 直接转换包装器扩展
 */
@Component
public class DirectCastWrapperExtension implements IFeatureWrapperExtension {

    @Override
    public <T> boolean support(@NotNull Object instance, @NotNull Class<T> feature) {
        return feature.isInstance(instance);
    }

    @Override
    public <T> T unwrap(@NotNull Object instance, @NotNull Class<T> feature) {
        return (T)instance;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
