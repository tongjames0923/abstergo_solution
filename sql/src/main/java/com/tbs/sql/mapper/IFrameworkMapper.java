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

/**
 * 框架通用Mapper接口，提供基于查询条件的通用CRUD操作。
 *
 * <p>该接口继承了tk.mybatis的多个Mapper接口，提供了通用的CRUD操作方法，
 * 并扩展了基于 {@link IQueryCriteria} 查询条件的查询和统计方法。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>继承BaseMapper，提供基础CRUD操作</li>
 *   <li>继承InsertListMapper，提供批量插入功能</li>
 *   <li>继承BatchUpdateMapper，提供批量更新功能</li>
 *   <li>继承BatchUpdateSelectiveMapper，提供选择性批量更新功能</li>
 *   <li>继承IdListMapper，提供根据ID列表查询功能</li>
 *   <li>提供基于查询条件的查询方法 {@link #queryByQO}</li>
 *   <li>提供基于查询条件的统计方法 {@link #countByQO}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>作为通用Mapper的基类使用</li>
 *   <li>需要基于查询条件进行查询时</li>
 *   <li>需要批量操作时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 定义实体Mapper
 * @Mapper
 * public interface UserMapper extends IFrameworkMapper<User, Long> {
 * }
 *
 * // 使用查询条件查询
 * UserQuery query = new UserQuery();
 * query.setStatus(1);
 * List<User> users = userMapper.queryByQO(query, null, User.class);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>需要正确配置tk.mybatis</li>
 *   <li>查询条件需要实现 {@link IQueryCriteria} 接口</li>
 *   <li>分页参数可以为null，表示不分页</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IQueryCriteria
 * @see ConditionBuilder
 * @see FetchMapper
 */
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

    default Integer countByQO(IQueryCriteria qo, Class<?> entityClass) {
        ImportedSqlBuildContext sqlContext = buildSqlContext(qo);
        EntitySqlDriver driver = new EntitySqlDriver();
        driver.setWhereInfo(ConditionBuilder.buildConditions(qo));
        return SpringUtil.getBean(FetchMapper.class).count(sqlContext, driver);
    }
}
