package com.tbs.sql.mapper;

import com.tbs.sql.criteria.IQueryCriteria;
import com.tbs.sql.dsl.IQueryBuilder;
import com.tbs.sql.mapper.impl.ConditionBuilder;
import com.tbs.sql.mapper.impl.FetchMapper;
import com.tbs.sql.service.QueryEngine;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.update.batch.BatchUpdateMapper;
import tk.mybatis.mapper.additional.update.batch.BatchUpdateSelectiveMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.BaseMapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

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
    BatchUpdateSelectiveMapper<T>, IdListMapper<T, K>, SmartQueryMapper {

    /**
     * 根据实体类类型获取查询构建器。
     *
     * @param entityClass 实体类类型
     * @return 查询构建器
     */
    default IQueryBuilder<T> query(Class<T> entityClass) {
        return QueryEngine.getEntityQueryWrapper(entityClass).query();
    }

    /**
     * 根据实体类对象获取查询构建器。
     *
     * @param entity 实体类对象
     * @return 查询构建器
     */
    default IQueryBuilder<T> query(T entity) {
        return QueryEngine.getEntityQueryWrapper(entity).query();
    }


}
