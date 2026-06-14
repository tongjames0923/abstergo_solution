package com.tbs.sql.annotation.core;

import java.lang.annotation.*;

/**
 * 直接SQL查询语句注解。
 *
 * <p>该注解用于在实体类上直接定义SQL查询语句，当实体类标注了此注解时，
 * 框架会优先使用注解中定义的SQL语句，而不是自动生成查询语句。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要执行复杂的JOIN查询时</li>
 *   <li>需要使用自定义SQL逻辑时</li>
 *   <li>需要查询多个表的关联数据时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @SelectSQL("SELECT u.*, d.name as dept_name FROM users u LEFT JOIN departments d ON u.dept_id = d.id")
 * public class UserWithDepartment {
 *     private Long id;
 *     private String name;
 *     private String deptName;
 *     // getter 和 setter 方法
 * }
 * 
 * // 使用自定义SQL查询实体
 * IEntityQueryWrapper<UserWithDepartment> wrapper = QueryEngine.getEntityQueryWrapper(UserWithDepartment.class);
 * List<UserWithDepartment> users = wrapper.query().fetch();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>该注解只能用于类级别</li>
 *   <li>当实体类标注了此注解时，框架会忽略自动生成的查询语句</li>
 *   <li>SQL语句中的字段名需要与实体类中的属性名匹配</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see QueryEngine
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface SelectSQL {
    String value() default "";
}
