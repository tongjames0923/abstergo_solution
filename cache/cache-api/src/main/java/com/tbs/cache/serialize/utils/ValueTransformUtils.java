package com.tbs.cache.serialize.utils;

import com.tbs.cache.CacheUtils;
import com.tbs.common.data.IDataBridge;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class ValueTransformUtils {
    @Data
    @AllArgsConstructor
    private static class TransformInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private Class<?> type;
        Class<? extends IDataBridge> bridgeType;
        IDataBridge bridge;
    }

    public static final int TRANSFORM_READ = 0;
    public static final int TRANSFORM_WRITE = 1;

    /**
     * 缓存值转换
     *
     * @param value 缓存值
     * @param mode  转换模式，TRANSFORM_READ表示读取缓存值，TRANSFORM_WRITE表示写入缓存值
     * @return 转换后的缓存值
     */
    public static Object valueTransform(@NotNull Object value, int mode) {
        Class<?> clazz = value.getClass();
        var info = CacheUtils.getInstance().computeIfAbsent("CACHE_TRANSFORM_INFO" + clazz.getName(), () -> {
            if (!clazz.isAnnotationPresent(com.tbs.cache.serialize.annotation.CacheTransform.class)) {
                return new TransformInfo(clazz, null, null);
            }
            var cacheTransform = clazz.getDeclaredAnnotation(com.tbs.cache.serialize.annotation.CacheTransform.class);
            return new TransformInfo(clazz, cacheTransform.bridgeType(),
                StrUtil.isEmpty(cacheTransform.bridgeName()) ? SpringUtil.getBean(cacheTransform.bridgeType())
                    : SpringUtil.getBean(cacheTransform.bridgeName(), cacheTransform.bridgeType()));
        }, TransformInfo.class).get();
        if (info.isPresent()) {
            var bridge = info.get().getBridge();
            if (bridge != null) {
                if (mode == TRANSFORM_WRITE) {
                    return bridge.bridge(value);
                } else if (mode == TRANSFORM_READ) {
                    return bridge.unbridge(value);
                }
            }
        }
        return value;
    }

}
