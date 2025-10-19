package com.tbs.common.util.framework;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Spring工具类
 *
 * @author abstergo
 */
@Slf4j
public class SpringUtil extends cn.hutool.extra.spring.SpringUtil {

	/**
	 * 获取bean，如果获取失败，则执行指定方法
	 *
	 * @param clazz
	 * @param throwableConsumer
	 * @param <T>
	 * @return
	 */
    public static <T> T getBeanSafe(@NotNull Class<T> clazz, @NotNull Consumer<Throwable> throwableConsumer) {
		try {
			return getBean(clazz);
		} catch (Throwable e) {
            log.debug("getBeans error", e);
			throwableConsumer.accept(e);
		}
		return null;
	}

	/**
	 * 获取bean，如果获取失败，则返回Optional.empty()
	 *
	 * @param clazz
	 * @param <T>
	 * @return
	 */
    public static <T> Optional<T> getBeanOptional(@NotNull Class<T> tClass) {
		try {
			return Optional.of(getBean(tClass));
		} catch (Exception e) {
            log.debug("getBeans error", e);
			return Optional.empty();
		}
	}

    /**
     * 根据类型获取bean
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Collection<T> getBeansByTypeSafe(@NotNull Class<T> clazz) {
        try {
            return SpringUtil.getBeansOfType(clazz).values();
        } catch (Exception e) {
            log.debug("getBeansByType error", e);
            return Collections.EMPTY_LIST;
        }
    }
}
