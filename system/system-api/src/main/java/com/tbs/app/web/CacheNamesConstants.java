package com.tbs.app.web;

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
     * 令牌签发中心中按 token 缓存的令牌模型
     */
    public static final String TOKEN_CACHE = "app:auth:tokenCache";

    /**
     * 按 token 缓存的用户登录详情
     */
    public static final String USER_LOGIN_INFO_CACHE = "app:auth:user_login_info_cache";

    /**
     * 按用户名缓存的用户会话列表
     */
    public static final String USER_SESSIONS_CACHE = "app:auth:user_sessions_cache";

    private CacheNamesConstants() {
    }
}
