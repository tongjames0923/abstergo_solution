package com.tbs.sql.provider.impl;

import com.tbs.sql.provider.IAutoValueProvider;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 日期自动值提供者实现类，用于自动填充日期时间字段。
 *
 * <p>该类实现了 {@link IAutoValueProvider} 接口，提供了对 {@link Date} 和 {@link LocalDateTime}
 * 类型的自动填充支持。当字段值为null时，自动填充当前时间。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>自动填充 {@link Date} 类型字段</li>
 *   <li>自动填充 {@link LocalDateTime} 类型字段</li>
 *   <li>仅在字段值为null时填充</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>自动填充创建时间字段</li>
 *   <li>自动填充更新时间字段</li>
 *   <li>需要记录操作时间的审计字段</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在实体类中使用
 * public class User {
 *     @AutoSetDate
 *     private Date createTime;
 *     
 *     @AutoSetDate
 *     private LocalDateTime updateTime;
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>仅支持 {@link Date} 和 {@link LocalDateTime} 类型</li>
 *   <li>仅在字段值为null时填充</li>
 *   <li>返回当前时间</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see IAutoValueProvider
 * @see com.tbs.sql.annotation.autofill.AutoSetDate
 */
public class DateValueAutoProvider implements IAutoValueProvider {
    @Override
    public Object getValue(Object target, Field field) {
        if (field.getType().equals(Date.class)) {
            return new Date();
        } else if (field.getType().equals(LocalDateTime.class)) {
            return LocalDateTime.now();
        } else {
            return null;
        }
    }

    @Override
    public boolean support(Class<?> type, Object oldValue) {
        return (Date.class.equals(type) || LocalDateTime.class.equals(type)) && oldValue == null;
    }
}
