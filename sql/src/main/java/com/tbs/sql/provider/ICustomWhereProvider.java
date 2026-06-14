package com.tbs.sql.provider;

/**
 * 自定义WHERE条件提供者接口，定义了自定义SQL条件的标准方法。
 *
 * <p>该接口定义了获取自定义SQL条件的方法，允许在查询条件中使用复杂的SQL逻辑。
 * 通常与 {@link com.tbs.sql.annotation.condition.QueryField} 注解的 customWhereProvider 属性配合使用。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>获取自定义SQL条件 {@link #getConditionSql(Object)}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要复杂的SQL条件逻辑时</li>
 *   <li>需要调用数据库函数时</li>
 *   <li>需要动态生成SQL条件时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 实现自定义WHERE条件提供者
 * @Component
 * public class CustomWhereProvider implements ICustomWhereProvider<String> {
 *     @Override
 *     public String getConditionSql(String value) {
 *         return "status = 1 AND is_active = '" + value + "'";
 *     }
 * }
 * 
 * // 在注解中使用
 * @QueryField(customWhereProvider = CustomWhereProvider.class)
 * private String customField;
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>实现类需要注册为Spring Bean</li>
 *   <li>返回的SQL片段会直接拼接到WHERE子句中</li>
 *   <li>注意SQL注入风险</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see com.tbs.sql.annotation.condition.QueryField
 */
public interface ICustomWhereProvider<T> {
    /**
     * 获取自定义的WHERE条件SQL片段。
     *
     * @param value 字段值
     * @return WHERE条件SQL片段，如 "status = 1 AND is_active = 'value'"
     */
    String getConditionSql(T value);
}
