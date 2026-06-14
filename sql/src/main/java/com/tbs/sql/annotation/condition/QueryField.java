package com.tbs.sql.annotation.condition;

import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.provider.ICustomWhereProvider;

import java.lang.annotation.*;

/**
 * 查询字段注解
 *
 * @author abstergo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Repeatable(QueryFields.class)
public @interface QueryField {
    /** 同一字段查询顺序，升序 */
    int index() default -1;
    /** 关系运算 */
    ConnectorEnum connector() default ConnectorEnum.AND;
    /** 比较运算 */
    ContrastEnum contrast() default ContrastEnum.EQUAL;
    /** 是否忽略大小写 */
    boolean ignoreCase() default false;
    /** 是否忽略空值 */
    boolean ignoreNull() default true;
    /** 自定义查询条件提供者 */
    Class<? extends ICustomWhereProvider> customWhereProvider() default ICustomWhereProvider.class;
}
