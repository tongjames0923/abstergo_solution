package com.tbs.common.util.helper;

import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.common.util.function.Comparators;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FeatureWrapperHelper implements IStartup {

    private static List<IFeatureWrapperExtension> extensions = Collections.emptyList();

    // 在类中增加缓存
    private static final Map<String, IFeatureWrapperExtension> extensionCache = new ConcurrentHashMap<>();

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

        // 生成缓存key
        String cacheKey = instance.getClass().getName() + "#" + feature.getName();

        // 尝试从缓存获取
        IFeatureWrapperExtension cachedExtension = extensionCache.get(cacheKey);
        if (cachedExtension != null) {
            if (cachedExtension.support(instance, feature)) {
                log.debug("从缓存中获取功能包装器 {}", cachedExtension);
                F result = cachedExtension.unwrap(instance, feature);
                if (result != null) {
                    return Optional.of(result);
                }
            }
            // 缓存失效，移除旧缓存
            extensionCache.remove(cacheKey);
        }
        // 遍历所有扩展，找到第一个支持且能解包出结果的扩展
        for (var extension : extensions) {
            if (extension.support(instance, feature)) {
                log.debug("找到功能包装器 {}", extension);
                F result = extension.unwrap(instance, feature);
                if (result != null) {
                    // 缓存结果
                    extensionCache.put(cacheKey, extension);
                    return Optional.of(result);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void startUp() throws RuntimeException {
        extensions = SpringUtil.getBeansByTypeSafe(IFeatureWrapperExtension.class).stream().sorted(Comparators::desc)
            .collect(Collectors.toList());
        log.info("加载功能包装器完成 {}", extensions);
    }

}
