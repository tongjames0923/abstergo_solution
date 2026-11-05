package com.tbs.sql.annotation.autofill;

import com.tbs.sql.provider.impl.DateValueAutoProvider;

import java.lang.annotation.*;

/**
 * 自动设置日期字段注解。
 *
 * <p>该注解用于自动填充实体类中的日期字段，在插入或更新操作时自动设置当前时间。
 * 支持 {@link java.util.Date} 和 {@link java.time.LocalDateTime} 类型。</p>
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
 *   <li>需要配合 {@link com.tbs.sql.interceptor.SqlAutoFillInterceptor} 使用</li>
 *   <li>在配置中启用 {@code tbs.sql.enable-auto-fill-value-interceptor: true}</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see SqlAutoValue
 * @see com.tbs.sql.provider.impl.DateValueAutoProvider
 */
@SqlAutoValue(value = DateValueAutoProvider.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoSetDate {
}
