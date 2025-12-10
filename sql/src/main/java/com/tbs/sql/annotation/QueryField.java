package com.tbs.sql.annotation;

import com.tbs.sql.enums.QueryConnectorEnum;
import com.tbs.sql.enums.QueryContrastEnum;
import com.tbs.sql.provider.ICustomWhereProvider;
import org.springframework.stereotype.Component;


import java.lang.annotation.*;

/**
 * 查询字段注解
 * <p>
 * 用于标记实体类中需要作为查询条件的字段
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Repeatable(QueryFields.class)
@Component
public @interface QueryField {

    /**
     * 同一字段查询顺序，升序
     * <p>当一个字段有多个查询条件时，用于指定条件的顺序</p>
     *
     * @return 查询条件的顺序
     */
    int index() default -1;

    /**
     * 关系运算
     * <p>指定当前查询条件与其他条件之间的逻辑关系</p>
     *
     * @return 逻辑关系运算符
     */
    QueryConnectorEnum connector() default QueryConnectorEnum.AND;

    /**
     * 比较运算
     * <p>指定字段的比较方式</p>
     *
     * @return 比较运算符
     */
    QueryContrastEnum contrast() default QueryContrastEnum.EQUAL;

    /**
     * 是否忽略大小写
     * <p>对于字符串类型的字段，指定是否忽略大小写进行比较</p>
     *
     * @return true表示忽略大小写，false表示不忽略
     */
    boolean ignoreCase() default false;

    /**
     * 是否忽略空值
     * <p>指定当字段值为空时是否忽略该查询条件</p>
     *
     * @return true表示忽略空值，false表示不忽略
     */
    boolean ignoreNull() default true;

//    /**
//     * 责任链模式进行值转化，通过spring bean获取
//     *
//     * @return class
//     */
//    Class<? extends AbstractConvertChainProvider> valueMapper() default BuiltInValueConvertChainProvider.class;

    /**
     * 自定义查询条件提供者
     * <p>指定自定义的查询条件提供者，使用自定义提供者时将会忽略所有其他设置</p>
     *
     * @return 自定义查询条件提供者类
     */
    Class<? extends ICustomWhereProvider> customWhereProvider() default ICustomWhereProvider.class;

}
