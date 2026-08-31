package com.tbs.cache.integration;

import org.springframework.cache.Cache;

import java.util.List;

/**
 * 默认多层缓存实现
 * <p>
 * 纯粹的编排实现，直接复用 {@link AbstractMultiLevelCache} 的全部语义。
 * TTL 能力由外层装饰（{@link AbstractAdaptTtlCache}）提供。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DefaultMultiLevelCache extends AbstractMultiLevelCache {

    /**
     * 构造函数
     *
     * @param name   缓存名称
     * @param levels 缓存层级列表（按 order 升序，index 0 为最快层）
     */
    public DefaultMultiLevelCache(String name, List<Cache> levels) {
        super(name, levels);
    }
}
