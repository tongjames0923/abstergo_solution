package com.tbs.sql.annotation.autofill;

import com.tbs.sql.provider.IAutoValueProvider;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * SQL自动填充值注解。
 *
 * <p>该注解用于标记需要自动填充值的字段，通过指定 {@link IAutoValueProvider} 实现类
 * 来提供自动填充逻辑。可以自定义值提供器以满足不同的业务需求。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>自动填充创建时间、更新时间等时间字段</li>
 *   <li>自动生成UUID、序列号等唯一标识</li>
 *   <li>根据业务规则自动计算字段值</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 使用默认提供器
 * @SqlAutoValue
 * private Date createTime;
 * 
 * // 使用自定义提供器
 * @SqlAutoValue(value = CustomValueProvider.class)
 * private String customField;
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>自定义提供器需要实现 {@link IAutoValueProvider} 接口</li>
 *   <li>需要配合 {@link com.tbs.sql.interceptor.SqlAutoFillInterceptor} 使用</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see AutoSetDate
 * @see IAutoValueProvider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlAutoValue {
    Class<? extends IAutoValueProvider> value() default IAutoValueProvider.class;
}
