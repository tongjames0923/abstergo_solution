package com.tbs.sql.annotation;

import com.tbs.sql.enums.QueryConnectorEnum;
import com.tbs.sql.enums.QueryContrastEnum;
import com.tbs.sql.provider.ICustomWhereProvider;
import org.springframework.stereotype.Component;


import java.lang.annotation.*;

/**
 * 查询字段注解
 *
 * @author abstergo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Repeatable(QueryFields.class)
@Component
public @interface QueryField {

    /**
     * 同一字段查询顺序，升序
     *
     * @return int
     */
    int index() default -1;

    /**
     * 关系运算
     *
     * @return query connector enum
     */
    QueryConnectorEnum connector() default QueryConnectorEnum.AND;

    /**
     * 比较运算
     *
     * @return query contrast enum
     */
    QueryContrastEnum contrast() default QueryContrastEnum.EQUAL;

    /**
     * 是否忽略大小写
     *
     * @return boolean
     */
    boolean ignoreCase() default false;

    /**
     * 是否忽略空值
     *
     * @return boolean
     */
    boolean ignoreNull() default true;

//    /**
//     * 责任链模式进行值转化，通过spring bean获取
//     *
//     * @return class
//     */
//    Class<? extends AbstractConvertChainProvider> valueMapper() default BuiltInValueConvertChainProvider.class;

    /**
     * 自定义查询条件,将会忽略所有设置
     *
     * @return class
     */
    Class<? extends ICustomWhereProvider> customWhereProvider() default ICustomWhereProvider.class;

}
