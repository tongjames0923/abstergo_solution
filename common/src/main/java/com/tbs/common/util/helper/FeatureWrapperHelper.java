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
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FeatureWrapperHelper implements IStartup {

    private static List<IFeatureWrapperExtension> extentions = Collections.EMPTY_LIST;

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

        Optional<F> result = Optional.empty();
        for (var extention : extentions) {
            if (extention.support(instance, feature)) {
                result = Optional.of(extention.unwrap(instance, feature));
                if (result.isPresent()) {
                    return result;
                }
            }
        }

        return result;
    }

    @Override
    public void startUp() throws RuntimeException {
        extentions = SpringUtil.getBeansByTypeSafe(IFeatureWrapperExtension.class).stream().sorted(Comparators::desc)
            .collect(Collectors.toList());
        log.info("加载功能包装器完成 {}", extentions);
    }

}
