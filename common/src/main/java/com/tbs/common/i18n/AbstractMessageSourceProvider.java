package com.tbs.common.i18n;

import org.jetbrains.annotations.NotNull;

/**
 * 消息源提供者抽象基类
 * <p>
 * 提供默认实现，简化各模块的使用。只需继承此类并实现 {@link #getBasename()} 方法即可。
 * <p>
 * 使用方式：
 * <pre>
 * &#64;Component
 * &#64;Order(100)
 * public class SystemModuleMessageSourceProvider extends AbstractMessageSourceProvider {
 *
 *     &#64;Override
 *     public String getBasename() {
 *         return "messages-system";
 *     }
 * }
 * </pre>
 *
 * @author abstergo
 * @see IMessageSourceProvider
 */
public abstract class AbstractMessageSourceProvider implements IMessageSourceProvider {

    /**
     * 默认优先级
     */
    private int order = 0;

    /**
     * 是否启用
     */
    private boolean enabled = true;

    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * 设置优先级
     *
     * @param order 优先级值，数字越小优先级越高
     */
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * 设置是否启用
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}