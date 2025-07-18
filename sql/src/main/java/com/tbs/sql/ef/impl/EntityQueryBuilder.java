package com.tbs.sql.ef.impl;

import com.tbs.common.function.SFunction;
import com.tbs.common.util.LambdaUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.sql.ef.IQueryOrderProvider;
import com.tbs.sql.ef.IQueryPageProvider;
import com.tbs.sql.ef.IQueryQueryBuilder;
import com.tbs.sql.ef.IQueryWhereProvider;
import com.tbs.sql.mapper.impl.FetchMapper;
import com.tbs.sql.model.FetchObject;
import com.tbs.sql.model.SqlGroupModel;
import com.tbs.sql.utils.SqlUtils;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author tongj
 */
public class EntityQueryBuilder<T> implements IQueryQueryBuilder<T> {
    private T entity;

    IQueryWhereProvider<T> whereProvider;

    IQueryOrderProvider<T> orderProvider;

    IQueryPageProvider<T> pageProvider;

    List<SqlGroupModel> groups = new LinkedList<>();

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
    public IQueryOrderProvider<T> getQueryOrderProvider() {
        return orderProvider;
    }

    @Override
    public IQueryPageProvider<T> getQueryPageProvider() {
        return pageProvider;
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
        return getFetchMapper().fetch(
            new FetchObject(entity, pageProvider.getLimit(), whereProvider.getCompose(), orderProvider.getSqlOrders(),
                groups));
    }

    @Override
    public T fetchOne() {
        return getFetchMapper().fetchOne(
            new FetchObject(entity, pageProvider.getLimit(), whereProvider.getCompose(), orderProvider.getSqlOrders(),
                groups));
    }

    @Override
    public Long count() {
        return getFetchMapper().count(
            new FetchObject(entity, pageProvider.getLimit(), whereProvider.getCompose(), orderProvider.getSqlOrders(),
                groups));
    }
}
