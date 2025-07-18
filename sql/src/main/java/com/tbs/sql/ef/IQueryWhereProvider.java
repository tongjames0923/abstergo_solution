package com.tbs.sql.ef;

import com.tbs.common.function.SFunction;
import com.tbs.sql.enums.QueryConnectorEnum;
import com.tbs.sql.model.SqlWhereCompose;

import java.util.function.Consumer;

/**
 * 条件提供者接口，用于提供条件配置
 *
 * @author tongj
 */
public interface IQueryWhereProvider<T> extends IFeatureProviderPour<T> {

    /**
     * 添加逻辑运算符AND，用于连接当前条件与下一个条件
     *
     * @return 返回自身，用于链式调用
     */
    IQueryWhereProvider<T> and();

    /**
     * 添加逻辑运算符OR，用于连接当前条件与下一个条件
     *
     * @return 返回自身，用于链式调用
     */
    IQueryWhereProvider<T> or();

    /**
     * 包含逻辑，即另起一组条件，常用于子查询或复杂查询条件的组织
     *
     * @param newCondition 消费者接口，用于定义新的条件
     * @return 返回自身，用于链式调用
     */
    IQueryWhereProvider<T> include(Consumer<IQueryWhereProvider<T>> newCondition,
        QueryConnectorEnum connectorEnum);

    /**
     * 添加逻辑运算符AND，用于连接当前条件与下一个条件
     *
     * @param newCondition 消费者接口，用于定义新的条件
     * @return 返回自身，用于链式调用
     */
    default IQueryWhereProvider<T> andInclude(Consumer<IQueryWhereProvider<T>> newCondition) {
        return include(newCondition, QueryConnectorEnum.AND);
    }

    /**
     * 添加逻辑运算符OR，用于连接当前条件与下一个条件
     *
     * @param newCondition 消费者接口，用于定义新的条件
     * @return 返回自身，用于链式调用
     */
    default IQueryWhereProvider<T> orInclude(Consumer<IQueryWhereProvider<T>> newCondition) {
        return include(newCondition, QueryConnectorEnum.OR);
    }

    /**
     * 判断字段是否为空
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> isNull(SFunction<T, P> acc);

    /**
     * 判断字段是否不为空
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> isNotNull(SFunction<T, P> acc);

    /**
     * 判断字段是否等于给定值
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> equal(SFunction<T, P> acc);

    /**
     * 判断字段是否等于给定值
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> equal(SFunction<T, P> acc, P value);

    /**
     * 判断字段是否小于给定值
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> lessThan(SFunction<T, P> acc);

    /**
     * 判断字段是否小于给定值
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> lessThan(SFunction<T, P> acc, P value);

    /**
     * 判断字段是否小于等于给定值
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> lessThanOrEqual(SFunction<T, P> acc);

    /**
     * 判断字段是否小于等于给定值
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> lessThanOrEqual(SFunction<T, P> acc, P value);

    /**
     * 判断字段是否大于给定值
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> greaterThan(SFunction<T, P> acc);

    /**
     * 判断字段是否大于给定值
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> greaterThan(SFunction<T, P> acc, P value);

    /**
     * 判断字段是否大于等于给定值
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> greaterThanOrEqual(SFunction<T, P> acc);

    /**
     * 判断字段是否大于等于给定值
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> greaterThanOrEqual(SFunction<T, P> acc, P value);

    /**
     * 判断字段是否不等于给定值
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> notEqual(SFunction<T, P> acc);

    /**
     * 判断字段是否不等于给定值
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> notEqual(SFunction<T, P> acc, P value);

    /**
     * 模糊查询，判断字段是否包含给定值
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> like(SFunction<T, P> acc);

    /**
     * 模糊查询，判断字段是否包含给定值
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> like(SFunction<T, P> acc, P value);

    /**
     * 左模糊查询，判断字段是否以给定值开头
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> leftLike(SFunction<T, P> acc);

    /**
     * 左模糊查询，判断字段是否以给定值开头
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> leftLike(SFunction<T, P> acc, P value);

    /**
     * 右模糊查询，判断字段是否以给定值结尾
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> rightLike(SFunction<T, P> acc);

    /**
     * 右模糊查询，判断字段是否以给定值结尾
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> rightLike(SFunction<T, P> acc, P value);

    /**
     * 不模糊查询，判断字段是否不包含给定值
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> notLike(SFunction<T, P> acc);

    /**
     * 不模糊查询，判断字段是否不包含给定值
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> notLike(SFunction<T, P> acc, P value);

    /**
     * IN查询，判断字段值是否在给定值列表中
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param val 给定的值列表
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> in(SFunction<T, P> acc, P val);

    /**
     * IN查询，判断字段值是否在给定值列表中
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> in(SFunction<T, P> acc);

    /**
     * NOT IN查询，判断字段值是否不在给定值列表中
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param val 给定的值列表
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> notIn(SFunction<T, P> acc, P val);

    /**
     * NOT IN查询，判断字段值是否不在给定值列表中
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> notIn(SFunction<T, P> acc);

    /**
     * 获取条件组合对象
     *
     * @return 返回条件组合对象
     */
    SqlWhereCompose getCompose();
}
