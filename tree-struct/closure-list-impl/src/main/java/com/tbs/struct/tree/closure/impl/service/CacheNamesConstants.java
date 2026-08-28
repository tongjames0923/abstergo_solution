package com.tbs.struct.tree.closure.impl.service;

/**
 * 缓存名称常量
 * <p>
 * 集中管理本模块 Spring Caching 注解使用的 cacheNames，禁止在注解中硬编码魔法值字符串。
 * </p>
 *
 * @see com.tbs.cache.config.TbsCacheProperty.TtlConfig#getOverrides() 对应 TTL 覆盖 key 需与本类取值保持一致
 */
public final class CacheNamesConstants {

    /**
     * 闭包表节点实体缓存（按 id）
     */
    public static final String TREE_NODE_ENTITY = "tree_node_entity";

    private CacheNamesConstants() {
    }
}
