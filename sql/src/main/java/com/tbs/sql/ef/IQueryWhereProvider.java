package com.tbs.sql.ef;

import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.sql.enums.QueryConnectorEnum;
import com.tbs.sql.framework.model.SqlWhereCompose;

/**
 * 条件提供者接口，用于提供SQL查询中的WHERE条件配置
 * <p>
 * 该接口提供了构建SQL WHERE子句的各种方法，包括比较操作、模糊查询、范围查询等。
 * 支持链式调用方式构建复杂的查询条件，并可通过逻辑运算符(AND、OR)组合多个条件。
 * </p>
 *
 * @param <T> 实体类类型
 * @author tongj
 * @see QueryConnectorEnum
 * @see SqlWhereCompose
 * @see SFunction
 */
public interface IQueryWhereProvider<T> extends IFeatureProviderPour<T> {

    /**
     * 添加逻辑运算符AND，用于连接当前条件与下一个条件
     * <p>
     * 在SQL查询中添加AND逻辑运算符，用于连接两个条件表达式。
     * 例如: WHERE column1 = 'value1' AND column2 = 'value2'
     * </p>
     *
     * @return 返回自身，用于链式调用
     */
    IQueryWhereProvider<T> and();

    /**
     * 添加逻辑运算符OR，用于连接当前条件与下一个条件
     * <p>
     * 在SQL查询中添加OR逻辑运算符，用于连接两个条件表达式。
     * 例如: WHERE column1 = 'value1' OR column2 = 'value2'
     * </p>
     *
     * @return 返回自身，用于链式调用
     */
    IQueryWhereProvider<T> or();

    /**
     * 添加逻辑运算符，用于连接当前条件与下一个条件
     * <p>
     * 通过指定的逻辑运算符(AND或OR)连接当前条件与新条件。
     * 支持嵌套条件，可构建复杂的查询条件组合。
     * </p>
     *
     * @param newCondition  消费者接口，用于定义新的条件组合
     * @param connectorEnum 逻辑运算符枚举，可以是AND或OR
     * @return 返回自身，用于链式调用
     * @see QueryConnectorEnum
     */
    IQueryWhereProvider<T> include(SConsumer<IQueryWhereProvider<T>> newCondition,
        QueryConnectorEnum connectorEnum);

    /**
     * 添加逻辑运算符AND，用于连接当前条件与下一个条件
     * <p>
     * 通过AND逻辑运算符连接当前条件与新条件。
     * 相当于调用include(newCondition, QueryConnectorEnum.AND)。
     * </p>
     *
     * @param newCondition 消费者接口，用于定义新的条件组合
     * @return 返回自身，用于链式调用
     */
    default IQueryWhereProvider<T> andInclude(SConsumer<IQueryWhereProvider<T>> newCondition) {
        return include(newCondition, QueryConnectorEnum.AND);
    }

    /**
     * 添加逻辑运算符OR，用于连接当前条件与下一个条件
     * <p>
     * 通过OR逻辑运算符连接当前条件与新条件。
     * 相当于调用include(newCondition, QueryConnectorEnum.OR)。
     * </p>
     *
     * @param newCondition 消费者接口，用于定义新的条件组合
     * @return 返回自身，用于链式调用
     */
    default IQueryWhereProvider<T> orInclude(SConsumer<IQueryWhereProvider<T>> newCondition) {
        return include(newCondition, QueryConnectorEnum.OR);
    }

    /**
     * 判断字段是否为空
     * <p>
     * 生成SQL语句: WHERE column IS NULL
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> isNull(SFunction<T, P> acc);

    /**
     * 判断字段是否不为空
     * <p>
     * 生成SQL语句: WHERE column IS NOT NULL
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> isNotNull(SFunction<T, P> acc);

    /**
     * 判断字段是否等于给定值
     * <p>
     * 生成SQL语句: WHERE column = ?
     * 参数值需要通过方法参数提供
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> equal(SFunction<T, P> acc);

    /**
     * 判断字段是否等于给定值
     * <p>
     * 生成SQL语句: WHERE column = 'value'
     * </p>
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值，用于比较
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> equal(SFunction<T, P> acc, P value);

    /**
     * 判断字段是否小于给定值
     * <p>
     * 生成SQL语句: WHERE column < ?
     * 参数值需要通过方法参数提供
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> lessThan(SFunction<T, P> acc);

    /**
     * 判断字段是否小于给定值
     * <p>
     * 生成SQL语句: WHERE column < 'value'
     * </p>
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值，用于比较
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> lessThan(SFunction<T, P> acc, P value);

    /**
     * 判断字段是否小于等于给定值
     * <p>
     * 生成SQL语句: WHERE column <= ?
     * 参数值需要通过方法参数提供
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> lessThanOrEqual(SFunction<T, P> acc);

    /**
     * 判断字段是否小于等于给定值
     * <p>
     * 生成SQL语句: WHERE column <= 'value'
     * </p>
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值，用于比较
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> lessThanOrEqual(SFunction<T, P> acc, P value);

    /**
     * 判断字段是否大于给定值
     * <p>
     * 生成SQL语句: WHERE column > ?
     * 参数值需要通过方法参数提供
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> greaterThan(SFunction<T, P> acc);

    /**
     * 判断字段是否大于给定值
     * <p>
     * 生成SQL语句: WHERE column > 'value'
     * </p>
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值，用于比较
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> greaterThan(SFunction<T, P> acc, P value);

    /**
     * 判断字段是否大于等于给定值
     * <p>
     * 生成SQL语句: WHERE column >= ?
     * 参数值需要通过方法参数提供
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> greaterThanOrEqual(SFunction<T, P> acc);

    /**
     * 判断字段是否大于等于给定值
     * <p>
     * 生成SQL语句: WHERE column >= 'value'
     * </p>
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值，用于比较
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> greaterThanOrEqual(SFunction<T, P> acc, P value);

    /**
     * 判断字段是否不等于给定值
     * <p>
     * 生成SQL语句: WHERE column <> ?
     * 参数值需要通过方法参数提供
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> notEqual(SFunction<T, P> acc);

    /**
     * 判断字段是否不等于给定值
     * <p>
     * 生成SQL语句: WHERE column <> 'value'
     * </p>
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值，用于比较
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> notEqual(SFunction<T, P> acc, P value);

    /**
     * 模糊查询，判断字段是否包含给定值
     * <p>
     * 生成SQL语句: WHERE column LIKE '%?%'
     * 参数值需要通过方法参数提供
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> like(SFunction<T, P> acc);

    /**
     * 模糊查询，判断字段是否包含给定值
     * <p>
     * 生成SQL语句: WHERE column LIKE '%value%'
     * </p>
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值，用于模糊匹配
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> like(SFunction<T, P> acc, P value);

    /**
     * 左模糊查询，判断字段是否以给定值开头
     * <p>
     * 生成SQL语句: WHERE column LIKE 'value%'
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> leftLike(SFunction<T, P> acc);

    /**
     * 左模糊查询，判断字段是否以给定值开头
     * <p>
     * 生成SQL语句: WHERE column LIKE 'value%'
     * </p>
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值，用于左模糊匹配
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> leftLike(SFunction<T, P> acc, P value);

    /**
     * 右模糊查询，判断字段是否以给定值结尾
     * <p>
     * 生成SQL语句: WHERE column LIKE '%value'
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> rightLike(SFunction<T, P> acc);

    /**
     * 右模糊查询，判断字段是否以给定值结尾
     * <p>
     * 生成SQL语句: WHERE column LIKE '%value'
     * </p>
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值，用于右模糊匹配
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> rightLike(SFunction<T, P> acc, P value);

    /**
     * 不模糊查询，判断字段是否不包含给定值
     * <p>
     * 生成SQL语句: WHERE column NOT LIKE '%?%'
     * 参数值需要通过方法参数提供
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> notLike(SFunction<T, P> acc);

    /**
     * 不模糊查询，判断字段是否不包含给定值
     * <p>
     * 生成SQL语句: WHERE column NOT LIKE '%value%'
     * </p>
     *
     * @param acc   函数式接口，用于访问实体类属性
     * @param value 给定的值，用于反向模糊匹配
     * @param <P>   实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> notLike(SFunction<T, P> acc, P value);

    /**
     * IN查询，判断字段值是否在给定值列表中
     * <p>
     * 生成SQL语句: WHERE column IN (?, ?, ...)
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param val 给定的值列表中的一个值
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> in(SFunction<T, P> acc, P val);

    /**
     * IN查询，判断字段值是否在给定值列表中
     * <p>
     * 生成SQL语句: WHERE column IN (?)
     * 值列表需要通过方法参数提供
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> in(SFunction<T, P> acc);

    /**
     * NOT IN查询，判断字段值是否不在给定值列表中
     * <p>
     * 生成SQL语句: WHERE column NOT IN (?, ?, ...)
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param val 给定的值列表中的一个值
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> notIn(SFunction<T, P> acc, P val);

    /**
     * NOT IN查询，判断字段值是否不在给定值列表中
     * <p>
     * 生成SQL语句: WHERE column NOT IN (?)
     * 值列表需要通过方法参数提供
     * </p>
     *
     * @param acc 函数式接口，用于访问实体类属性
     * @param <P> 实体类属性类型
     * @return 返回自身，用于链式调用
     */
    <P> IQueryWhereProvider<T> notIn(SFunction<T, P> acc);

    /**
     * 获取条件组合对象
     * <p>
     * 返回封装了所有WHERE条件的组合对象，用于后续SQL语句的生成。
     * </p>
     *
     * @return 返回条件组合对象
     * @see SqlWhereCompose
     */
    SqlWhereCompose getCompose();
}
