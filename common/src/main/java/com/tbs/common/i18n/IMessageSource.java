package com.tbs.common.i18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * 消息源接口，提供国际化消息获取能力
 * <p>
 * 基础包不强制依赖Spring，由使用方决定具体实现方式
 *
 * @author abstergo
 */
public interface IMessageSource {

    /**
     * 获取国际化消息
     *
     * @param code 消息代码
     * @return 消息内容，如果找不到则返回代码本身
     */
    
    String getMessage( String code);

    /**
     * 获取国际化消息
     *
     * @param code   消息代码
     * @param args   消息参数，用于替换占位符 {0}, {1} 等
     * @return 消息内容，如果找不到则返回代码本身
     */
    
    String getMessage( String code, @Nullable Object[] args);

    /**
     * 获取国际化消息
     *
     * @param code           消息代码
     * @param args           消息参数，用于替换占位符 {0}, {1} 等
     * @param defaultMessage 默认消息，如果找不到消息时返回
     * @return 消息内容
     */
    
    String getMessage( String code, @Nullable Object[] args, @Nullable String defaultMessage);

    /**
     * 获取国际化消息
     *
     * @param code   消息代码
     * @param args   消息参数，用于替换占位符 {0}, {1} 等
     * @param locale 指定的语言区域
     * @return 消息内容，如果找不到则返回代码本身
     */
    
    String getMessage( String code, @Nullable Object[] args,  Locale locale);

    /**
     * 获取国际化消息
     *
     * @param code           消息代码
     * @param args           消息参数，用于替换占位符 {0}, {1} 等
     * @param defaultMessage 默认消息，如果找不到消息时返回
     * @param locale         指定的语言区域
     * @return 消息内容
     */
    
    String getMessage( String code, @Nullable Object[] args, @Nullable String defaultMessage,  Locale locale);

    /**
     * 判断是否存在指定代码的消息
     *
     * @param code 消息代码
     * @return 是否存在
     */
    boolean hasMessage( String code);

    /**
     * 判断是否存在指定代码的消息
     *
     * @param code   消息代码
     * @param locale 指定的语言区域
     * @return 是否存在
     */
    boolean hasMessage( String code,  Locale locale);
}
