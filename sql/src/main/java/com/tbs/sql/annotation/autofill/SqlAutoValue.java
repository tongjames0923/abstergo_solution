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
 * // INSERT和UPDATE都填充
 * @SqlAutoValue
 * private Date createTime;
 *
 * // 使用自定义提供器
 * @SqlAutoValue(value = CustomValueProvider.class)
 * private String customField;
 *
 * // 仅INSERT时填充（如ID字段）
 * @SqlAutoValue(value = SnowflakeIdAutoProvider.class, insertOnly = true)
 * private Long id;
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>自定义提供器需要实现 {@link IAutoValueProvider} 接口</li>
 *   <li>需要配合 {@link com.tbs.sql.interceptor.SqlAutoFillInterceptor} 使用</li>
 *   <li>{@code insertOnly = true} 时，仅在INSERT操作时填充，UPDATE操作跳过</li>
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
    /**
     * 自动值提供者实现类。
     *
     * @return 提供者类，默认为 {@link IAutoValueProvider}（无操作）
     */
    Class<? extends IAutoValueProvider> value() default IAutoValueProvider.class;

    /**
     * 是否仅在INSERT操作时自动填充。
     *
     * <p>默认为false，表示INSERT和UPDATE操作都会触发自动填充。
     * 设为true时，仅在INSERT操作时填充，UPDATE操作时跳过该字段。</p>
     *
     * <p>SQL类型通过MyBatis底层 {@link org.apache.ibatis.mapping.MappedStatement#getSqlCommandType()}
     * 获取，不依赖SQL字符串解析，确保判断准确。</p>
     *
     * <h3>典型用法</h3>
     * <ul>
     *   <li>ID字段：{@code @SqlAutoValue(value = SnowflakeIdAutoProvider.class, insertOnly = true)}</li>
     *   <li>创建时间：{@code @SqlAutoValue(value = DateValueAutoProvider.class, insertOnly = true)}</li>
     *   <li>更新时间：{@code @SqlAutoValue(value = DateValueAutoProvider.class)} — INSERT和UPDATE都填充</li>
     * </ul>
     *
     * @return 是否仅INSERT时填充
     */
    boolean insertOnly() default false;

    /** true=仅字段为null时才填充，已有值不覆盖 */
    boolean fillWhenNull() default true;
}
