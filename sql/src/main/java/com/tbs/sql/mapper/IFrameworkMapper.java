package com.tbs.sql.mapper;

import static com.tbs.sql.mapper.impl.FrameworkSelectProvider.getImportedSqlContext;

import java.util.List;

import com.tbs.common.util.SpringUtil;
import com.tbs.sql.framework.driver.impl.EntityQuerySqlDriver;
import com.tbs.sql.framework.driver.impl.ImportedSqlContext;
import com.tbs.sql.framework.model.SqlLimitModel;
import com.tbs.sql.mapper.impl.FetchMapper;
import com.tbs.sql.mapper.impl.FrameworkSelectProvider;
import com.tbs.sql.query.IPage;
import com.tbs.sql.query.IQueryCriteria;
import com.tbs.sql.utils.QueryUtil;

import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.update.batch.BatchUpdateMapper;
import tk.mybatis.mapper.additional.update.batch.BatchUpdateSelectiveMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.BaseMapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

/**
 * @author abstergo
 */
@RegisterMapper
public interface IFrameworkMapper<T, K> extends BaseMapper<T>, InsertListMapper<T>, BatchUpdateMapper<T>,
		BatchUpdateSelectiveMapper<T>, IdListMapper<T, K> {

	default List<T> queryByQO(IQueryCriteria qo, IPage page, Class<T> entityClass) {
		ImportedSqlContext sqlContext = getImportedSqlContext(entityClass);
		EntityQuerySqlDriver driver = new EntityQuerySqlDriver();
		driver.setWhereInfo(FrameworkSelectProvider.buildConditions(qo));
		if (page != null) {
			driver.setLimit(new SqlLimitModel(page.from(), page.number()));
		}
		var orders = QueryUtil.collectOrderInfos(qo);
		driver.setOrders(orders);
		return SpringUtil.getBean(FetchMapper.class).<T>fetch(sqlContext, driver, entityClass);
	}

	default Long countByQO(IQueryCriteria qo,Class<?> entityClass) {
		ImportedSqlContext sqlContext = getImportedSqlContext(entityClass);
		EntityQuerySqlDriver driver = new EntityQuerySqlDriver();
		driver.setWhereInfo(FrameworkSelectProvider.buildConditions(qo));
		return SpringUtil.getBean(FetchMapper.class).count(sqlContext, driver);
	}
}
