package com.tbs.sql.ef.impl;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.tbs.common.function.SFunction;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.LambdaUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.sql.ef.IQueryOrderProvider;
import com.tbs.sql.ef.IQueryPageProvider;
import com.tbs.sql.ef.IQueryQueryBuilder;
import com.tbs.sql.ef.IQueryWhereProvider;
import com.tbs.sql.framework.driver.impl.EntityQuerySqlDriver;
import com.tbs.sql.framework.driver.impl.ImportedSqlContext;
import com.tbs.sql.framework.model.SqlGroupModel;
import com.tbs.sql.mapper.impl.FetchMapper;
import com.tbs.sql.utils.SqlUtils;

/**
 * @author tongj
 */
public class EntityQueryBuilder<T> implements IQueryQueryBuilder<T> {
	private T entity;

	IQueryWhereProvider<T> whereProvider;

	IQueryOrderProvider<T> orderProvider;

	IQueryPageProvider<T> pageProvider;

	List<SqlGroupModel> groups = new LinkedList<>();

	public static <T> IQueryPageProvider<T> ONE_LIMIT(IQueryQueryBuilder<T> builder) {
		return new EntityPageProvider<>(builder).limit(0L, 1L);
	}

	private FetchMapper getFetchMapper() {
		return SpringUtil.getBean(FetchMapper.class);
	}

	public EntityQueryBuilder(T entity) {
		this.entity = entity;
		whereProvider = new EntityQueryWhereProvider<>(this);
		orderProvider = new EntityQueryOrderProvider<>(this);
		pageProvider = new EntityPageProvider<>(this);
	}

	@Override
	public T getEntity() {
		return entity;
	}

	@Override
	public <P> IQueryQueryBuilder<T> access(BiConsumer<T, P> accessor, P property) {
		accessor.accept(entity, property);
		return this;
	}

	@Override
	public IQueryQueryBuilder<T> op(Consumer<T> operator) {
		operator.accept(entity);
		return this;
	}

	@Override
	public IQueryWhereProvider<T> where() {
		return whereProvider;
	}

	@Override
	public Optional<IQueryOrderProvider<T>> getOrderSupport() {
		return Optional.ofNullable(orderProvider);
	}

	@Override
	public Optional<IQueryPageProvider<T>> getPageLimitSupport() {
		return Optional.ofNullable(pageProvider);
	}

	@Override
	public <P> IQueryQueryBuilder<T> groupBy(SFunction<T, P> accessor) {
		Field field = LambdaUtil.getField(accessor);
		SqlGroupModel sqlGroupModel = new SqlGroupModel();
		sqlGroupModel.setFieldName(SqlUtils.getFiledNameWithMapping(field));
		groups.add(sqlGroupModel);
		return this;
	}

	@Override
	public List<T> fetch() {
		ImportedSqlContext sqlContext = new ImportedSqlContext(() -> entity);
		sqlContext.rebuild();
		EntityQuerySqlDriver driver = new EntityQuerySqlDriver();
		driver.setSqlGroups(groups);
		driver.setWhereInfo(whereProvider.getCompose());
		driver.setLimit(pageProvider.getLimit());
		driver.setOrders(orderProvider.getSqlOrders());
		return getFetchMapper().<T>fetch(sqlContext, driver, (Class<T>) entity.getClass());
	}

	@Override
	public T fetchOne() {
		ImportedSqlContext sqlContext = new ImportedSqlContext(() -> entity);
		sqlContext.rebuild();
		EntityQuerySqlDriver driver = new EntityQuerySqlDriver();
		driver.setSqlGroups(groups);
		driver.setWhereInfo(whereProvider.getCompose());
		driver.setOrders(orderProvider.getSqlOrders());
		driver.setLimit(ONE_LIMIT(this).getLimit());
		return CollUtil.first(getFetchMapper().<T>fetch(sqlContext, driver, (Class<T>) entity.getClass()));
	}

	@Override
	public Long count() {
		ImportedSqlContext sqlContext = new ImportedSqlContext(() -> entity);
		sqlContext.rebuild();
		EntityQuerySqlDriver driver = new EntityQuerySqlDriver();
		driver.setSqlGroups(groups);
		driver.setWhereInfo(whereProvider.getCompose());
		driver.setLimit(pageProvider.getLimit());
		return getFetchMapper().count(sqlContext, driver);
	}
}
