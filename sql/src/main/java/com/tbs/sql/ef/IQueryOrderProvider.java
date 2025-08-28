package com.tbs.sql.ef;


import com.tbs.common.function.SFunction;
import com.tbs.sql.enums.QueryOrderEnum;
import com.tbs.sql.framework.model.SqlOrderInfo;

import java.util.List;

/**
 * 排序提供者接口，用于提供SQL查询中的排序功能
 * <p>
 * 该接口提供了对查询结果进行排序的方法，支持按指定字段的升序或降序排列。
 * 可以通过函数式接口方式指定排序字段，并支持链式调用添加多个排序条件。
 * </p>
 *
 * @param <T> 实体类类型
 * @author abstergo
 * @see QueryOrderEnum
 * @see SqlOrderInfo
 * @see SFunction
 */
public interface IQueryOrderProvider<T> extends IFeatureProviderPour<T> {
    /**
     * 添加排序条件
     * <p>
     * 根据指定的实体属性和排序方向添加排序条件。
     * 生成SQL语句中的ORDER BY子句，如: ORDER BY column ASC 或 ORDER BY column DESC。
     * 支持链式调用，可添加多个排序条件。
     * </p>
     *
     * @param accessor 函数式接口，用于访问实体类属性，指定排序字段
     * @param order    排序枚举值，指定排序方向(升序ASC或降序DESC)
     * @param <P>      实体类属性类型
     * @return 返回自身，用于链式调用
     * @see SFunction
     * @see QueryOrderEnum
     */
    <P> IQueryOrderProvider<T> orderBy(SFunction<T, P> accessor, QueryOrderEnum order);

    /**
     * 倒序排序
     * <p>
     * 根据指定的实体属性按降序(DESC)添加排序条件。
     * 相当于调用orderBy(accessor, QueryOrderEnum.DESC)。
     * 生成SQL语句: ORDER BY column DESC
     * </p>
     *
     * @param accessor 函数式接口，用于访问实体类属性，指定排序字段
     * @param <P>      实体类属性类型
     * @return 返回自身，用于链式调用
     * @see QueryOrderEnum
     */
    default <P> IQueryOrderProvider<T> orderByDesc(SFunction<T, P> accessor) {
        return orderBy(accessor, QueryOrderEnum.DESC);
    }

    /**
     * 正序排序
     * <p>
     * 根据指定的实体属性按升序(ASC)添加排序条件。
     * 相当于调用orderBy(accessor, QueryOrderEnum.ASC)。
     * 生成SQL语句: ORDER BY column ASC
     * </p>
     *
     * @param accessor 函数式接口，用于访问实体类属性，指定排序字段
     * @param <P>      实体类属性类型
     * @return 返回自身，用于链式调用
     * @see QueryOrderEnum
     */
    default <P> IQueryOrderProvider<T> orderByAsc(SFunction<T, P> accessor) {
        return orderBy(accessor, QueryOrderEnum.ASC);
    }

    /**
     * 获取排序信息
     * <p>
     * 返回所有已添加的排序条件列表，每个条件封装在SqlOrderInfo对象中。
     * 用于后续SQL语句中ORDER BY子句的生成。
     * </p>
     *
     * @return 排序信息列表
     * @see SqlOrderInfo
     * @see List
     */
    List<SqlOrderInfo> getSqlOrders();

}
