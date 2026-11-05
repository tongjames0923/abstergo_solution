package com.tbs.sql.annotation.core;

import java.lang.annotation.*;

/**
 * 直接SQL查询语句注解。
 *
 * <p>该注解用于在实体类上直接定义SQL查询语句，当实体类标注了此注解时，
 * 框架会优先使用注解中定义的SQL语句，而不是自动生成查询语句。</p>
 *
 * <p>更推荐把复杂 SQL 外置为 classpath 资源文件（继承 {@code AbstractResourceCriteria}），
 * 便于按库型维护与审阅；本注解适合与实体强绑定的简短自定义查询。</p>
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
 *   <li>该注解只标注在类上，由 {@code EntityMetadataExtractor.pullFromClass()} 从类元数据读取</li>
 *   <li>命中后框架不再自动生成 SELECT，也不再要求实体标注 {@code @Table}</li>
 *   <li>SQL 内<b>不要自带 WHERE</b>：DSL 条件、逻辑删除与数据权限拦截器产生的条件会被追加到该 SQL 之后</li>
 *   <li>列投影（include/exclude）对自定义 SQL 无效，此时的字段清单为 null</li>
 *   <li>只读取类自身声明的注解，标注在父类上对子类不生效</li>
 *   <li>SQL语句中的字段名需要与实体类中的属性名匹配，或显式起别名</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see com.tbs.sql.service.QueryEngine
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface SelectSQL {
    String value() default "";
}
