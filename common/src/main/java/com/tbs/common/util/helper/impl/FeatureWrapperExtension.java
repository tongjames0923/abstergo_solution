package com.tbs.common.util.helper.impl;

import com.tbs.common.data.IFeatureWrapper;
import com.tbs.common.util.helper.FeatureWrapperHelper;
import com.tbs.common.util.helper.IFeatureWrapperExtension;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class FeatureWrapperExtension implements IFeatureWrapperExtension {
    @Override
    public <T> boolean support(@NotNull Object instance, @NotNull Class<T> feature) {
        return instance instanceof IFeatureWrapper;
    }

    @Override
    public <T> T unwrap(@NotNull Object instance, @NotNull Class<T> feature) {
        // 防止无限递归的情况
        Object original = ((IFeatureWrapper<?>)instance).getOrginal();
        if (original == instance) {
            // 如果原始对象就是自身，避免无限递归
            return null;
        }
        return FeatureWrapperHelper.unwrap(original, feature).orElse(null);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
