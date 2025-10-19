package com.tbs.sql.ef.impl;

import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.function.LambdaUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.sql.ef.IQueryOrderProvider;
import com.tbs.sql.ef.IQueryPageProvider;
import com.tbs.sql.ef.IQueryQueryBuilder;
import com.tbs.sql.ef.IQueryWhereProvider;
import com.tbs.sql.framework.driver.impl.EntityQuerySqlDriver;
import com.tbs.sql.framework.driver.impl.ImportedSqlContext;
import com.tbs.sql.framework.model.SqlGroupModel;
import com.tbs.sql.mapper.impl.FetchMapper;
import com.tbs.sql.utils.SqlUtils;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * 实体查询构建器实现类，用于构建和执行针对实体对象的SQL查询
 * <p>
 * 该类是IQueryQueryBuilder接口的具体实现，提供了完整的查询构建和执行功能。
 * 包括WHERE条件构建、排序设置、分页设置、分组设置等功能，并最终执行查询操作。
 * </p>
 *
 * @param <T> 实体类类型
 * @author tongj
 * @see IQueryQueryBuilder
 * @see EntityQueryWhereProvider
 * @see EntityQueryOrderProvider
 * @see EntityPageProvider
 */
public class EntityQueryBuilder<T> implements IQueryQueryBuilder<T> {
    /**
     * FetchMapper实例缓存
     */
    private static volatile FetchMapper fetchMapper;
    /**
     * 分组模型列表，用于存储查询的分组条件
	 */
	List<SqlGroupModel> groups = new LinkedList<>();
    /**
     * 实体对象，用于存储查询相关的实体信息
     */
	private T entity;
    /**
     * WHERE条件提供者，用于构建查询的WHERE条件
     */
    private IQueryWhereProvider<T> whereProvider;
    /**
     * 排序提供者，用于构建查询的排序条件
     */
    private IQueryOrderProvider<T> orderProvider;
    /**
     * 分页提供者，用于构建查询的分页条件
     */
    private IQueryPageProvider<T> pageProvider;

    /**
     * 构造函数，初始化实体查询构建器
	 *
	 * @param entity 实体对象
	 */
	public EntityQueryBuilder(T entity) {
        this.entity = entity;
	}

    /**
     * 创建一个限制只返回一条记录的分页提供者
     *
     * @param builder 查询构建器实例
     * @param <T>     实体类类型
     * @return 分页提供者，限制只返回一条记录
     */
    public static <T> IQueryPageProvider<T> topOne(IQueryQueryBuilder<T> builder) {
		return new EntityPageProvider<>(builder).limit(0L, 1L);
    }

    /**
     * 获取数据获取映射器
	 *
	 * @return FetchMapper实例
     */
    private FetchMapper getFetchMapper() {
        if (fetchMapper == null) {
            synchronized (EntityQueryBuilder.class) {
                if (fetchMapper == null) {
                    fetchMapper = SpringUtil.getBean(FetchMapper.class);
                }
            }
        }
        return fetchMapper;
    }

	/**
	 * 获取实体对象
	 *
	 * @return 实体对象
	 */
	@Override
    public T getEntity() {
        return entity;
    }

    /**
     * 访问实体对象的属性设置访问器
     * <p>
     * 通过SBiConsumer函数式接口设置实体对象的特定属性值。
     * </p>
     *
     * @param accessor  双参数消费者接口，用于设置实体属性
     * @param property  要设置的属性值
     * @param <P>       属性值类型
     * @return 返回自身，用于链式调用
     * @see SBiConsumer
     */
    @Override
    public <P> IQueryQueryBuilder<T> access(SBiConsumer<T, P> accessor, P property) {
        if (entity != null) {
			accessor.accept(entity, property);
        }
        return this;
    }

    /**
     * 直接操作内部实体对象
     * <p>
     * 通过SConsumer函数式接口直接对内部实体对象进行操作。
     * </p>
     *
     * @param operator  单参数消费者接口，用于操作实体对象
	 * @return 返回自身，用于链式调用
     * @see SConsumer
     */
    @Override
    public IQueryQueryBuilder<T> op(SConsumer<T> operator) {
        if (entity != null) {
            operator.accept(entity);
        }
        return this;
    }

    /**
     * 获取where条件提供者
     * <p>
     * 返回WHERE条件构建器，用于构建查询的WHERE子句。
     * </p>
     *
     * @return WHERE条件提供者接口实例
	 * @see IQueryWhereProvider
	 */
	@Override
    public IQueryWhereProvider<T> where() {
        if (whereProvider == null) {
            whereProvider = new EntityQueryWhereProvider<>(this);
        }
        return whereProvider;
    }

    /**
     * 获取排序提供者
     * <p>
     * 返回排序构建器的可选包装，用于构建查询的ORDER BY子句。
     * </p>
     *
     * @return 包含排序提供者的Optional包装
     * @see Optional
	 * @see IQueryOrderProvider
	 */
	@Override
	public Optional<IQueryOrderProvider<T>> getOrderSupport() {
        if (orderProvider == null) {
            orderProvider = new EntityQueryOrderProvider<>(this);
        }
        return Optional.of(orderProvider);
    }

    /**
     * 获取分页提供者
     * <p>
     * 返回分页构建器的可选包装，用于构建查询的LIMIT/OFFSET子句。
     * </p>
     *
     * @return 包含分页提供者的Optional包装
     * @see Optional
	 * @see IQueryPageProvider
	 */
	@Override
	public Optional<IQueryPageProvider<T>> getPageLimitSupport() {
        if (pageProvider == null) {
            pageProvider = new EntityPageProvider<>(this);
        }
        return Optional.of(pageProvider);
    }

    /**
     * 分组
     * <p>
     * 添加GROUP BY子句到查询中，根据指定的实体属性进行分组。
     * </p>
     *
     * @param accessor  函数式接口，用于访问实体类属性
     * @param <P>       实体类属性类型
	 * @return 返回自身，用于链式调用
	 * @see SFunction
	 */
	@Override
	public <P> IQueryQueryBuilder<T> groupBy(SFunction<T, P> accessor) {
        if (entity != null) {
            Field field = LambdaUtil.getField(accessor);
            SqlGroupModel sqlGroupModel = new SqlGroupModel();
            sqlGroupModel.setFieldName(SqlUtils.getFiledNameWithMapping(field));
			groups.add(sqlGroupModel);
        }
        return this;
    }

    /**
     * 获取查询结果列表
     * <p>
     * 执行查询操作并返回所有符合条件的结果列表。
     * </p>
     *
     * @return 查询结果列表，包含所有符合条件的实体对象
	 * @see List
     */
    @Override
    public List<T> fetch() {
        if (entity == null) {
            return CollUtil.newArrayList();
        }
		EntityQuerySqlDriver driver = createDriver();
		ImportedSqlContext sqlContext = new ImportedSqlContext(() -> entity);
        sqlContext.rebuild();
        configureDriver(driver);
        return getFetchMapper().<T>fetch(sqlContext, driver, (Class<T>)entity.getClass());
    }

    /**
     * 获取查询第一个结果
     * <p>
     * 执行查询操作并返回第一条符合条件的结果。
     * </p>
	 *
	 * @return 查询结果中的第一个实体对象，如果无结果则返回null
     */
    @Override
    public T fetchOne() {
        if (entity == null) {
            return null;
        }
		EntityQuerySqlDriver driver = createDriver();
		ImportedSqlContext sqlContext = new ImportedSqlContext(() -> entity);
        sqlContext.rebuild();
        configureDriver(driver);
        driver.setLimit(topOne(this).getLimit());
        return CollUtil.first(getFetchMapper().<T>fetch(sqlContext, driver, (Class<T>)entity.getClass()));
    }

    /**
     * 获取查询结果数量
     * <p>
     * 执行COUNT查询操作，返回符合条件的记录总数。
	 * </p>
	 *
	 * @return 符合条件的记录总数
     */
    @Override
    public Long count() {
        if (entity == null) {
            return 0L;
        }
		EntityQuerySqlDriver driver = createDriver();
		ImportedSqlContext sqlContext = new ImportedSqlContext(() -> entity);
        sqlContext.rebuild();
		configureDriver(driver);
		return getFetchMapper().count(sqlContext, driver);
    }

    /**
     * 创建EntityQuerySqlDriver实例
     *
     * @return EntityQuerySqlDriver实例
     */
    private EntityQuerySqlDriver createDriver() {
        return new EntityQuerySqlDriver();
    }

    /**
     * 配置EntityQuerySqlDriver的通用属性
     *
     * @param driver 要配置的驱动程序
     */
    private void configureDriver(EntityQuerySqlDriver driver) {
        driver.setSqlGroups(groups);
        driver.setWhereInfo(whereProvider != null ? whereProvider.getCompose() : null);
        driver.setLimit(pageProvider != null ? pageProvider.getLimit() : null);
        driver.setOrders(orderProvider != null ? orderProvider.getSqlOrders() : null);
	}
}
