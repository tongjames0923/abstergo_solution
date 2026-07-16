package com.tbs.sql.mapper;

import com.tbs.common.util.framework.SpringUtil;
import com.tbs.sql.builder.EntitySqlDriver;
import com.tbs.sql.builder.ImportedSqlBuildContext;
import com.tbs.sql.criteria.IPage;
import com.tbs.sql.criteria.IQueryCriteria;
import com.tbs.sql.mapper.impl.ConditionBuilder;
import com.tbs.sql.mapper.impl.FetchMapper;
import com.tbs.sql.model.limit.LimitModel;
import com.tbs.sql.service.QueryEngine;
import tk.mybatis.mapper.annotation.RegisterMapper;

import java.util.List;

import static com.tbs.sql.mapper.impl.ConditionBuilder.buildSqlContext;

@RegisterMapper
public interface SmartQueryMapper {
    /**
     * 根据查询条件、分页参数和实体类查询实体列表。
     *
     * <p>该方法构建SQL上下文和驱动，设置查询条件、分页限制和排序信息，
     * 最后通过 {@link FetchMapper} 执行查询并返回结果列表。</p>
     *
     * @param qo          查询条件对象，实现 {@link IQueryCriteria} 接口
     * @param page        分页参数，可以为null表示不分页
     * @param entityClass 实体类类型
     * @return 查询结果列表
     */
    default <T> List<T> queryByQO(IQueryCriteria qo, IPage page, Class<T> entityClass) {
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

    /**
     * 根据查询条件和实体类统计符合条件的记录数。
     *
     * <p>该方法构建SQL上下文和驱动，设置查询条件，
     * 最后通过 {@link FetchMapper} 执行统计查询并返回记录数。</p>
     *
     * @param qo          查询条件对象，实现 {@link IQueryCriteria} 接口
     * @param entityClass 实体类类型
     * @return 符合条件的记录数
     */
    default <T> Integer countByQO(IQueryCriteria qo, Class<T> entityClass) {
        ImportedSqlBuildContext sqlContext = buildSqlContext(qo);
        EntitySqlDriver driver = new EntitySqlDriver();
        driver.setWhereInfo(ConditionBuilder.buildConditions(qo));
        return SpringUtil.getBean(FetchMapper.class).count(sqlContext, driver);
    }
}
