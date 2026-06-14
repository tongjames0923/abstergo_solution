package com.tbs.sql.mapper;

import com.tbs.common.util.framework.SpringUtil;
import com.tbs.sql.builder.EntitySqlDriver;
import com.tbs.sql.builder.ImportedSqlBuildContext;
import com.tbs.sql.criteria.IPage;
import com.tbs.sql.criteria.IQueryCriteria;
import com.tbs.sql.mapper.impl.ConditionBuilder;
import com.tbs.sql.mapper.impl.FetchMapper;
import com.tbs.sql.model.limit.LimitModel;
import com.tbs.sql.model.order.OrderInfo;
import com.tbs.sql.service.QueryEngine;
import org.jetbrains.annotations.NotNull;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.update.batch.BatchUpdateMapper;
import tk.mybatis.mapper.additional.update.batch.BatchUpdateSelectiveMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.BaseMapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

import java.util.List;

import static com.tbs.sql.mapper.impl.ConditionBuilder.buildSqlContext;

@RegisterMapper
public interface IFrameworkMapper<T, K> extends BaseMapper<T>, InsertListMapper<T>, BatchUpdateMapper<T>,
        BatchUpdateSelectiveMapper<T>, IdListMapper<T, K> {

    default List<T> queryByQO(IQueryCriteria qo, IPage page, Class<T> entityClass) {
        ImportedSqlBuildContext sqlContext = buildSqlContext(qo);
        EntitySqlDriver driver = new EntitySqlDriver();
        driver.setWhereInfo(ConditionBuilder.buildConditions(qo));
        if (page != null) {
            driver.setLimit(new LimitModel(page.from(), page.number()));
        }
        var orders = QueryEngine.collectOrderInfos(qo);
        driver.setOrders(orders);
        return SpringUtil.getBean(FetchMapper.class).<T>fetch(sqlContext, driver, entityClass);
    }

    default Long countByQO(IQueryCriteria qo, Class<?> entityClass) {
        ImportedSqlBuildContext sqlContext = buildSqlContext(qo);
        EntitySqlDriver driver = new EntitySqlDriver();
        driver.setWhereInfo(ConditionBuilder.buildConditions(qo));
        return SpringUtil.getBean(FetchMapper.class).count(sqlContext, driver);
    }
}
