package com.tbs.app.service.impl;

/**
 * 缓存名称常量
 * <p>
 * 集中管理 default-system-impl 模块 Spring Caching 注解使用的 cacheNames，禁止在注解中硬编码魔法值字符串。
 * </p>
 *
 * @see com.tbs.cache.config.TbsCacheProperty.TtlConfig#getOverrides() 对应 TTL 覆盖 key 需与本类取值保持一致
 */
public final class CacheNamesConstants {

    // ===== 用户 =====
    /**
     * 系统用户实体缓存（按 id）
     */
    public static final String SYSTEM_USER = "app:system_user";

    /**
     * 用户名到用户 id 映射缓存
     */
    public static final String USER_NAME_ID_MAP = "app:user_name_id_map";

    /**
     * 用户名到用户详情映射缓存
     */
    public static final String USER_NAME_TO_ID = "app:user_name_to_id";

    // ===== 角色 =====
    /**
     * 系统角色实体缓存（按 id）
     */
    public static final String SYSTEM_ROLE = "app:system_role";

    /**
     * 按角色编码缓存的角色实体
     */
    public static final String SYSTEM_ROLE_NAME = "app:system_role_name";

    // ===== 角色组 =====
    /**
     * 角色组下权限列表缓存（按 groupId）
     */
    public static final String ROLE_GROUP = "app:roleGroup";

    /**
     * 角色组下用户列表缓存（按 userId）
     */
    public static final String ROLE_GROUP_USER_ID = "app:role_group_user_id";

    /**
     * 公司查询缓存
     */
    public static final String COMPANY_QUERY_ID = "app:company_query_id";


    // ===== 令牌 =====
    /**
     * 令牌实体缓存（按 token）
     */
    public static final String TOKEN_ENTITY = "app:token_entity";

    // ===== 应用版本兼容 =====
    /**
     * 应用版本兼容性策略缓存
     */
    public static final String APP_VERSION_COMPATIBILITY = "app:app_version_compatibility";

    // ===== 视图 =====
    /**
     * 视图表单配置缓存（按 id）
     */
    public static final String VIEW_FORM_CACHE = "app:view_form_cache";

    // ===== 部门 =====
    /**
     * 用户所属部门列表缓存（按 userId）
     */
    public static final String USER_DEPARTMENTS = "app:user_departments";

    /**
     * 通用 API 缓存
     */
    public static final String GENERIC_API = "app:generic_api";

    /**
     * 泛化API入参定义缓存（按 apiId 缓存整组参数，新版泛化调用链路的元信息来源）
     */
    public static final String GENERIC_API_PARAM = "app:generic_api_param";

    // ===== 企业微信 =====
    /**
     * 企业微信应用 access_token 缓存（单应用单 token）
     */
    public static final String WECOM_ACCESS_TOKEN = "app:wecom_access_token";

    // ===== 条件规则 =====
    /**
     * 条件规则实体缓存（按 id）
     */
    public static final String CONDITION_RULE = "app:condition_rule";

    /**
     * 条件规则按编码缓存（按 ruleCode）
     */
    public static final String CONDITION_RULE_CODE = "app:condition_rule_code";

    // ===== 事件动作绑定 =====
    /**
     * 事件动作绑定实体缓存（按 id）
     */
    public static final String EVENT_ACTION = "app:event_action";

    /**
     * 事件动作绑定按事件缓存（按 reporterId + eventCode，含启用/非启用两套键）
     */
    public static final String EVENT_ACTION_EVENT = "app:event_action_event";

    // ===== 事件订阅 =====
    /**
     * 事件订阅实体缓存（按 id）
     */
    public static final String EVENT_SUBSCRIPTION = "app:event_subscription";

    /**
     * 事件订阅按事件缓存（按 reporterId + eventCode，含启用/非启用两套键）
     */
    public static final String EVENT_SUBSCRIPTION_EVENT = "app:event_subscription_event";

    /**
     * 事件订阅按事件与触发器缓存（按 reporterId + eventCode + triggerId）
     */
    public static final String EVENT_SUBSCRIPTION_TRIGGER = "app:event_subscription_trigger";

    private CacheNamesConstants() {
    }
}
