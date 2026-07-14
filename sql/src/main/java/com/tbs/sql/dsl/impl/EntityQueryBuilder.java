package com.tbs.sql.dsl.impl;

import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.common.util.function.LambdaUtil;
import com.tbs.sql.builder.EntitySqlDriver;
import com.tbs.sql.builder.ImportedSqlBuildContext;
import com.tbs.sql.builder.SqlWithParams;
import com.tbs.sql.dsl.IOrderClause;
import com.tbs.sql.dsl.IPageClause;
import com.tbs.sql.dsl.IQueryBuilder;
import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.mapper.impl.FetchMapper;
import com.tbs.sql.model.group.GroupModel;
import com.tbs.sql.service.QueryEngine;
import com.tbs.sql.util.SqlUtils;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class EntityQueryBuilder<T> implements IQueryBuilder<T> {
    List<GroupModel> groups = new LinkedList<>();
    private T entity;
    private IWhereClause<T> whereProvider;
    private IOrderClause<T> orderProvider;
    private IPageClause<T> pageProvider;

    public EntityQueryBuilder(T entity) { this.entity = entity; }

    public static <T> IPageClause<T> topOne(IQueryBuilder<T> builder) {
        return new EntityPageClause<>(builder).limit(0L, 1L);
    }

    private FetchMapper getFetchMapper() { return SpringUtil.getBean(FetchMapper.class); }

    @Override public T getEntity() { return entity; }

    @Override
    public <P> IQueryBuilder<T> access(SBiConsumer<T, P> accessor, P property) {
        if (entity != null) accessor.accept(entity, property);
        return this;
    }

    @Override
    public IQueryBuilder<T> op(SConsumer<T> operator) {
        if (entity != null) operator.accept(entity);
        return this;
    }

    @Override
    public IWhereClause<T> where() {
        if (whereProvider == null) whereProvider = new EntityWhereClause<>(this);
        return whereProvider;
    }

    @Override
    public Optional<IOrderClause<T>> getOrderSupport() {
        if (orderProvider == null) orderProvider = new EntityOrderClause<>(this);
        return Optional.of(orderProvider);
    }

    @Override
    public Optional<IPageClause<T>> getPageLimitSupport() {
        if (pageProvider == null) pageProvider = new EntityPageClause<>(this);
        return Optional.of(pageProvider);
    }

    @Override
    public <P> IQueryBuilder<T> groupBy(SFunction<T, P> accessor) {
        if (entity != null) {
            Field field = LambdaUtil.getField(accessor);
            GroupModel groupModel = new GroupModel();
            groupModel.setFieldName(SqlUtils.getFieldNameWithMapping(field));
            groups.add(groupModel);
        }
        return this;
    }

    @Override
    public List<T> fetch() {
        if (entity == null) {
            return CollUtil.newArrayList();
        }
        EntitySqlDriver driver = createDriver();
        ImportedSqlBuildContext sqlContext = new ImportedSqlBuildContext(() -> entity);
        sqlContext.rebuild();
        configureDriver(driver);
        mergeSubQueryParams(sqlContext);
        return getFetchMapper().<T>fetch(sqlContext, driver, (Class<T>) entity.getClass());
    }

    @Override
    public T fetchOne() {
        if (entity == null) {
            return null;
        }
        EntitySqlDriver driver = createDriver();
        ImportedSqlBuildContext sqlContext = new ImportedSqlBuildContext(() -> entity);
        sqlContext.rebuild();
        configureDriver(driver);
        mergeSubQueryParams(sqlContext);
        driver.setLimit(topOne(this).getLimit());
        return CollUtil.of(getFetchMapper().<T>fetch(sqlContext, driver, (Class<T>) entity.getClass())).findFirstOrNull();
    }

    @Override
    public Integer count() {
        if (entity == null) {
            return 0;
        }
        EntitySqlDriver driver = createDriver();
        ImportedSqlBuildContext sqlContext = new ImportedSqlBuildContext(() -> entity);
        sqlContext.rebuild();
        configureDriver(driver);
        mergeSubQueryParams(sqlContext);
        return getFetchMapper().count(sqlContext, driver);
    }

    private EntitySqlDriver createDriver() { return new EntitySqlDriver(); }

    private void configureDriver(EntitySqlDriver driver) {
        driver.setSqlGroups(groups);
        driver.setWhereInfo(whereProvider != null ? whereProvider.getCompose() : null);
        driver.setLimit(pageProvider != null ? pageProvider.getLimit() : null);
        driver.setOrders(orderProvider != null ? orderProvider.getSqlOrders() : null);
    }

    @Override
    public String toSql() {
        if (entity == null) {
            return "SELECT 1 WHERE 1=0";
        }
        EntitySqlDriver driver = createDriver();
        ImportedSqlBuildContext sqlContext = new ImportedSqlBuildContext(() -> entity);
        sqlContext.rebuild();
        configureDriver(driver);
        mergeSubQueryParams(sqlContext);
        return QueryEngine.getInstance().driveSql(sqlContext, driver);
    }

    @Override
    public SqlWithParams toSqlWithParams() {
        if (entity == null) {
            return new SqlWithParams("SELECT 1 WHERE 1=0", new java.util.HashMap<>());
        }
        EntitySqlDriver driver = createDriver();
        ImportedSqlBuildContext sqlContext = new ImportedSqlBuildContext(() -> entity);
        sqlContext.rebuild();
        configureDriver(driver);
        mergeSubQueryParams(sqlContext);
        String sql = QueryEngine.getInstance().driveSql(sqlContext, driver);
        return new SqlWithParams(sql, sqlContext.getSqlParams());
    }

    /**
     * 合并子查询参数到SQL上下文。
     *
     * @param sqlContext SQL构建上下文
     */
    private void mergeSubQueryParams(ImportedSqlBuildContext sqlContext) {
        if (whereProvider instanceof EntityWhereClause) {
            EntityWhereClause<?> whereClause = (EntityWhereClause<?>) whereProvider;
            sqlContext.getSqlParams().putAll(whereClause.getSubQueryParams());
        }
    }
}
