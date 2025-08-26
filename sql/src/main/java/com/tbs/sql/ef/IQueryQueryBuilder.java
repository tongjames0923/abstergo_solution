package com.tbs.sql.ef;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.tbs.common.function.SFunction;

/**
 * @author tongj
 */
public interface IQueryQueryBuilder<T> extends IQueryFetcher<T> {

	/**
	 * 获取实体对象
	 *
	 * @return
	 */
	T getEntity();

	/**
	 * 访问实体对象的属性设置访问器
	 *
	 * @param accessor
	 * @param property
	 * @param <P>
	 * @return
	 */
	<P> IQueryQueryBuilder<T> access(BiConsumer<T, P> accessor, P property);

	/**
	 * 直接操作内部实体对象
	 *
	 * @param operator
	 * @return
	 */
	IQueryQueryBuilder<T> op(Consumer<T> operator);

	/**
	 * 获取where条件提供者
	 *
	 * @return
	 */
	IQueryWhereProvider<T> where();

	/**
	 * 获取排序提供者
	 *
	 * @return
	 */
	Optional<IQueryOrderProvider<T>> getOrderSupport();

	/**
	 * 获取分页提供者
	 *
	 * @return
	 */
	Optional<IQueryPageProvider<T>> getPageLimitSupport();

	/**
	 * 分组
	 *
	 * @param accessor
	 * @param <P>
	 * @return
	 */
	<P> IQueryQueryBuilder<T> groupBy(SFunction<T, P> accessor);

}
