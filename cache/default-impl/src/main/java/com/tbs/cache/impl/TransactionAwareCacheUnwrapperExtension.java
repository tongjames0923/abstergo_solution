package com.tbs.cache.impl;

import com.tbs.common.util.helper.FeatureWrapperHelper;
import com.tbs.common.util.helper.IFeatureWrapperExtension;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.stereotype.Component;

@Component
public class TransactionAwareCacheUnwrapperExtension implements IFeatureWrapperExtension {
    @Override
    public <T> boolean support(@NotNull Object instance, @NotNull Class<T> feature) {
        return instance instanceof TransactionAwareCacheDecorator;
    }

    @Override
    public <T> T unwrap(@NotNull Object instance, @NotNull Class<T> feature) {
        var cache = (TransactionAwareCacheDecorator)instance;
        return FeatureWrapperHelper.unwrap(cache.getTargetCache(), feature).orElse(null);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
