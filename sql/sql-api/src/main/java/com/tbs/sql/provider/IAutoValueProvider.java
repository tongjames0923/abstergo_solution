package com.tbs.sql.provider;

import com.tbs.sql.provider.impl.DateValueAutoProvider;

import java.lang.reflect.Field;

/**
 * 自动值提供者接口，定义了自动填充字段值的标准方法。
 *
 * <p>该接口定义了获取自动填充值的方法，允许在SQL执行前自动填充字段值。
 * 通常与 {@link com.tbs.sql.annotation.autofill.SqlAutoValue} 注解配合使用。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>获取自动填充值 {@link #getValue(Object, Field)}</li>
 *   <li>判断是否支持类型 {@link #support(Class, Object)}</li>
 * </ul>
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
 * // 实现自动值提供者
 * @Component
 * public class CustomAutoProvider implements IAutoValueProvider {
 *     @Override
 *     public Object getValue(Object target, Field field) {
 *         return "auto_value";
 *     }
 *
 *     @Override
 *     public boolean support(Class<?> type, Object oldValue) {
 *         return String.class.equals(type) && oldValue == null;
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>实现类需要注册为Spring Bean</li>
 *   <li>在 {@link #support} 方法中判断是否需要填充</li>
 *   <li>返回null表示不填充</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see com.tbs.sql.annotation.autofill.SqlAutoValue
 * @see DateValueAutoProvider
 */
public interface IAutoValueProvider {
    /**
     * 获取自动填充值。
     *
     * @param target 目标实体对象
     * @param field 需要填充的字段
     * @return 自动填充的值，返回null表示不填充
     */
    Object getValue(Object target, Field field);

    /**
     * 判断是否支持指定类型的值填充。
     *
     * @param type 字段类型
     * @param oldValue 字段当前值
     * @return 如果支持填充返回true，否则返回false
     */
    boolean support(Class<?> type, Object oldValue);
}
