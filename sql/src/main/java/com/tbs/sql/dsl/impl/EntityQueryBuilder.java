package com.tbs.sql.dsl.impl;

import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.common.util.function.LambdaUtil;
import com.tbs.sql.builder.EntitySqlDriver;
import com.tbs.sql.builder.ImportedISqlBuildContext;
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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EntityQueryBuilder<T> implements IQueryBuilder<T> {
    List<GroupModel> groups = new LinkedList<>();
    private T entity;
    private IWhereClause<T> whereProvider;
    private IOrderClause<T> orderProvider;
    private IPageClause<T> pageProvider;
    /** 投影：仅返回的列（物理列名），为空表示不限制 */
    private Set<String> includeColumns;
    /** 投影：需排除的列（物理列名），为空表示不排除 */
    private Set<String> excludeColumns;

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
    public <P> IQueryBuilder<T> select(SFunction<T, P> accessor) {
        if (entity != null) {
            if (includeColumns == null) {
                includeColumns = new LinkedHashSet<>();
            }
            includeColumns.add(resolveColumnName(accessor));
        }
        return this;
    }

    @Override
    public <P> IQueryBuilder<T> exclude(SFunction<T, P> accessor) {
        if (entity != null) {
            if (excludeColumns == null) {
                excludeColumns = new LinkedHashSet<>();
            }
            excludeColumns.add(resolveColumnName(accessor));
        }
        return this;
    }

    @SafeVarargs
    @Override
    public final IQueryBuilder<T> selectAny(SFunction<T, ?>... accessors) {
        if (accessors != null) {
            for (SFunction<T, ?> accessor : accessors) {
                select(accessor);
            }
        }
        return this;
    }

    @SafeVarargs
    @Override
    public final IQueryBuilder<T> excludeAny(SFunction<T, ?>... accessors) {
        if (accessors != null) {
            for (SFunction<T, ?> accessor : accessors) {
                exclude(accessor);
            }
        }
        return this;
    }

    /**
     * 将字段引用解析为物理列名，与 {@link com.tbs.sql.metadata.EntityMetadataExtractor} 产出的 fieldName 对齐。
     */
    private <P> String resolveColumnName(SFunction<T, P> accessor) {
        return SqlUtils.getFieldNameWithMapping(LambdaUtil.getField(accessor));
    }

    @Override
    public List<T> fetch() {
        if (entity == null) {
            return CollUtil.newArrayList();
        }
        EntitySqlDriver driver = createDriver();
        ImportedISqlBuildContext sqlContext = new ImportedISqlBuildContext(() -> entity);
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
        ImportedISqlBuildContext sqlContext = new ImportedISqlBuildContext(() -> entity);
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
        ImportedISqlBuildContext sqlContext = new ImportedISqlBuildContext(() -> entity);
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
        driver.setProjection(includeColumns, excludeColumns);
    }

    @Override
    public String toSql() {
        if (entity == null) {
            return "SELECT 1 WHERE 1=0";
        }
        EntitySqlDriver driver = createDriver();
        ImportedISqlBuildContext sqlContext = new ImportedISqlBuildContext(() -> entity);
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
        driver.setMainQuery(false); // 子查询不使用main_别名
        ImportedISqlBuildContext sqlContext = new ImportedISqlBuildContext(() -> entity);
        sqlContext.rebuild();
        configureDriver(driver);
        mergeSubQueryParams(sqlContext);
        String sql = QueryEngine.getInstance().driveSql(sqlContext, driver);
        return new SqlWithParams(sql, sqlContext.getSqlParams());
    }

    /**
     * 合并子查询参数到SQL上下文。
     *
     * <p>子查询和父查询都有独立的参数计数器，参数键可能冲突（如都是p0）。
     * 因此在合并时，需要重新映射子查询的参数键，使其与SQL中的占位符一致。</p>
     *
     * @param sqlContext SQL构建上下文
     */
    private void mergeSubQueryParams(ImportedISqlBuildContext sqlContext) {
        if (whereProvider instanceof EntityWhereClause) {
            EntityWhereClause<?> whereClause = (EntityWhereClause<?>) whereProvider;
            sqlContext.getSqlParams().putAll(whereClause.getSubQueryParams());
        }
    }
}
