package com.tbs.sql.ef;

import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 查询构建器接口，用于构建和执行SQL查询
 * <p>
 * 该接口提供了完整的查询构建功能，包括实体访问、条件设置、排序、分页和分组等操作。
 * 作为查询的核心接口，它继承了IQueryFetcher接口，具备数据获取能力。
 * </p>
 *
 * @param <T> 实体类类型
 * @author tongj
 * @see IQueryFetcher
 * @see IQueryWhereProvider
 * @see IQueryOrderProvider
 * @see IQueryPageProvider
 */
public interface IQueryQueryBuilder<T> extends IQueryFetcher<T> {

	/**
	 * 获取实体对象
     * <p>
     * 返回当前查询构建器关联的实体对象，可用于直接访问或修改实体属性。
     * </p>
	 *
     * @return 实体对象实例
	 */
	T getEntity();

	/**
	 * 访问实体对象的属性设置访问器
     * <p>
     * 通过BiConsumer函数式接口设置实体对象的特定属性值。
     * 这种方式可以更灵活地操作实体属性，避免直接暴露实体对象的内部结构。
     * </p>
	 *
     * @param accessor  双参数消费者接口，用于设置实体属性
     * @param property  要设置的属性值
     * @param <P>       属性值类型
     * @return 返回自身，用于链式调用
     * @see BiConsumer
	 */
    <P> IQueryQueryBuilder<T> access(SBiConsumer<T, P> accessor, P property);

	/**
	 * 直接操作内部实体对象
     * <p>
     * 通过Consumer函数式接口直接对内部实体对象进行操作。
     * 提供了一种更直接的方式来自定义实体对象的处理逻辑。
     * </p>
	 *
     * @param operator  单参数消费者接口，用于操作实体对象
     * @return 返回自身，用于链式调用
     * @see Consumer
	 */
    IQueryQueryBuilder<T> op(SConsumer<T> operator);

	/**
	 * 获取where条件提供者
     * <p>
     * 返回WHERE条件构建器，用于构建查询的WHERE子句。
     * 通过该方法可以添加各种查询条件，如等于、大于、模糊匹配等。
     * </p>
	 *
     * @return WHERE条件提供者接口实例
     * @see IQueryWhereProvider
	 */
	IQueryWhereProvider<T> where();

	/**
	 * 获取排序提供者
     * <p>
     * 返回排序构建器的可选包装，用于构建查询的ORDER BY子句。
     * 如果当前查询支持排序功能，则返回包含排序提供者的Optional；
     * 否则返回空的Optional。
     * </p>
	 *
     * @return 包含排序提供者的Optional包装
     * @see Optional
     * @see IQueryOrderProvider
	 */
	Optional<IQueryOrderProvider<T>> getOrderSupport();

	/**
	 * 获取分页提供者
     * <p>
     * 返回分页构建器的可选包装，用于构建查询的LIMIT/OFFSET子句。
     * 如果当前查询支持分页功能，则返回包含分页提供者的Optional；
     * 否则返回空的Optional。
     * </p>
	 *
     * @return 包含分页提供者的Optional包装
     * @see Optional
     * @see IQueryPageProvider
	 */
	Optional<IQueryPageProvider<T>> getPageLimitSupport();

	/**
	 * 分组
     * <p>
     * 添加GROUP BY子句到查询中，根据指定的实体属性进行分组。
     * 常用于聚合查询场景，如COUNT、SUM、AVG等聚合函数配合使用。
     * </p>
	 *
     * @param accessor  函数式接口，用于访问实体类属性
     * @param <P>       实体类属性类型
     * @return 返回自身，用于链式调用
     * @see SFunction
	 */
	<P> IQueryQueryBuilder<T> groupBy(SFunction<T, P> accessor);

}
