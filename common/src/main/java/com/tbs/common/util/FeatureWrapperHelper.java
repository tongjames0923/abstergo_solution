package com.tbs.common.util;

import com.tbs.common.data.IFeatureWrapper;
import com.tbs.common.error.AssertUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FeatureWrapperHelper {

    /**
     * 解包
     *
     * @param instance 实例对象，可能为null
     * @param feature  目标特征类型，不能为null
     * @param <F>      特征类型
     * @return 解包后的特征对象Optional
     */
    public static <F> Optional<F> unwrap(@NotNull Object instance, @NotNull Class<F> feature) {
        AssertUtil.notNull(feature, "目标类型不能为空");
        if (instance == null) {
            return Optional.empty();
        }
        if (feature.isInstance(instance)) {
            return Optional.of(feature.cast(instance));
        }
        if (instance instanceof IFeatureWrapper) {
            // 防止无限递归的情况
            Object original = ((IFeatureWrapper<?>)instance).getOrginal();
            if (original == instance) {
                // 如果原始对象就是自身，避免无限递归
                return Optional.empty();
            }
            return unwrap(original, feature);
        }
        return Optional.empty();
    }

}
