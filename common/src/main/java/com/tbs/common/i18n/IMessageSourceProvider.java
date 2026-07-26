package com.tbs.common.i18n;

import org.jetbrains.annotations.NotNull;

/**
 * 消息源提供者接口
 * <p>
 * 各模块通过实现此接口并注册为 Spring Bean，自动将其 basename 注册到国际化系统中。
 * <p>
 * 使用方式：
 * <pre>
 * &#64;Component
 * public class SystemModuleMessageSourceProvider implements IMessageSourceProvider {
 *     &#64;Override
 *     public String getBasename() {
 *         return "messages-system";
 *     }
 *
 *     &#64;Override
 *     public int getOrder() {
 *         return 100; // 数字越小优先级越高
 *     }
 * }
 * </pre>
 * <p>
 * 或使用便捷基类 {@link AbstractMessageSourceProvider}
 *
 * @author abstergo
 * @see AbstractMessageSourceProvider
 */
public interface IMessageSourceProvider {

    /**
     * 获取消息资源 basename
     * <p>
     * 对应模块 resources 目录下的 .properties 文件名（不含后缀）
     * 例如返回 "messages-system"，则对应 messages-system.properties 文件
     *
     * @return basename
     */
    
    String getBasename();

    /**
     * 获取优先级（用于多模块 basename 冲突时的处理）
     * <p>
     * 数字越小优先级越高，默认为 0
     * 当多个模块定义了相同的 key 时，优先级高的模块的消息会被使用
     *
     * @return 优先级值
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 是否启用此消息源
     *
     * @return 是否启用
     */
    default boolean isEnabled() {
        return true;
    }
}
