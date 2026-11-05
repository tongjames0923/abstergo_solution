package com.tbs.sql.datasource.model;

/**
 * 拦截裁决结果，由拦截器返回，框架据此放行或拒绝执行。
 *
 * <p>拒绝时拦截器只需提供 i18n 资源 key 与参数，框架统一经
 * {@code AssertUtil} 断言抛出异常，拦截器实现方不自行构造异常。</p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class InterceptVerdict {

    /**
     * 默认拒绝文案资源 key（拦截器未提供 key 时兜底）
     */
    public static final String DEFAULT_DENY_KEY = "datasource.intercept.rejected";

    private static final InterceptVerdict ALLOW = new InterceptVerdict(true, null, null);

    private final boolean allowed;

    private final String i18nKey;

    private final Object[] params;

    private InterceptVerdict(boolean allowed, String i18nKey, Object[] params) {
        this.allowed = allowed;
        this.i18nKey = i18nKey;
        this.params = params;
    }

    /**
     * 放行裁决。
     *
     * @return 放行单例
     */
    public static InterceptVerdict allow() {
        return ALLOW;
    }

    /**
     * 拒绝裁决，携带 i18n 资源 key 与消息参数。
     *
     * @param i18nKey 拒绝文案资源 key（为空时使用 {@link #DEFAULT_DENY_KEY} 兜底）
     * @param params  消息参数，按 {@code {0}} 占位符顺序填充
     * @return 拒绝裁决
     */
    public static InterceptVerdict deny(String i18nKey, Object... params) {
        return new InterceptVerdict(false, i18nKey, params);
    }

    /**
     * 是否为放行裁决。
     *
     * @return true 表示放行
     */
    public boolean isAllowed() {
        return allowed;
    }

    /**
     * 是否为拒绝裁决。
     *
     * @return true 表示拒绝
     */
    public boolean isDenied() {
        return !allowed;
    }

    /**
     * 获取拒绝文案资源 key。
     *
     * @return i18n key，放行裁决返回 null
     */
    public String getI18nKey() {
        return i18nKey;
    }

    /**
     * 获取拒绝文案参数。
     *
     * @return 消息参数数组，放行裁决返回 null
     */
    public Object[] getParams() {
        return params;
    }
}
