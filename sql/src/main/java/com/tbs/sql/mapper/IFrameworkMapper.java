package com.tbs.sql.mapper;

import com.tbs.common.util.framework.SpringUtil;
import com.tbs.sql.ef.IEntityQueryWrapper;
import com.tbs.sql.ef.IQueryQueryBuilder;
import com.tbs.sql.framework.driver.impl.EntityQuerySqlDriver;
import com.tbs.sql.framework.driver.impl.ImportedSqlContext;
import com.tbs.sql.framework.model.SqlLimitModel;
import com.tbs.sql.mapper.impl.FetchMapper;
import com.tbs.sql.mapper.impl.FrameworkSelectProvider;
import com.tbs.sql.query.IPage;
import com.tbs.sql.query.IQueryCriteria;
import com.tbs.sql.utils.QueryUtil;
import org.jetbrains.annotations.NotNull;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.update.batch.BatchUpdateMapper;
import tk.mybatis.mapper.additional.update.batch.BatchUpdateSelectiveMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.BaseMapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

import java.util.List;

import static com.tbs.sql.mapper.impl.FrameworkSelectProvider.buildSqlContext;

/**
 * @author abstergo
 */
@RegisterMapper
public interface IFrameworkMapper<T, K> extends BaseMapper<T>, InsertListMapper<T>, BatchUpdateMapper<T>,
		BatchUpdateSelectiveMapper<T>, IdListMapper<T, K> {

    /**
     * 获取查询包装器
     *
     * @param entity 实体对象
     * @return 查询包装器
     */
    default IQueryQueryBuilder<T> getQueryWrapper(@NotNull T entity) {
        return SpringUtil.getBean(IEntityQueryWrapper.class).makeQueryable(entity).query();
    }

    /**
     * 根据查询条件查询
     *
     * @param qo 查询条件
     * @return 查询结果
     */
	default List<T> queryByQO(IQueryCriteria qo, IPage page, Class<T> entityClass) {
		ImportedSqlContext sqlContext = buildSqlContext(qo);
		EntityQuerySqlDriver driver = new EntityQuerySqlDriver();
		driver.setWhereInfo(FrameworkSelectProvider.buildConditions(qo));
		if (page != null) {
			driver.setLimit(new SqlLimitModel(page.from(), page.number()));
		}
		var orders = QueryUtil.collectOrderInfos(qo);
		driver.setOrders(orders);
		return SpringUtil.getBean(FetchMapper.class).<T>fetch(sqlContext, driver, entityClass);
    }

    /**
     * 根据查询条件查询
     *
     * @param qo 查询条件
     * @return 统计结果
     */
	default Long countByQO(IQueryCriteria qo,Class<?> entityClass) {
		ImportedSqlContext sqlContext = buildSqlContext(qo);
		EntityQuerySqlDriver driver = new EntityQuerySqlDriver();
		driver.setWhereInfo(FrameworkSelectProvider.buildConditions(qo));
		return SpringUtil.getBean(FetchMapper.class).count(sqlContext, driver);
	}
}
