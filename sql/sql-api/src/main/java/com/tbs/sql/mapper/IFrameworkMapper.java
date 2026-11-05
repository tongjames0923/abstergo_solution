package com.tbs.sql.mapper;

import com.tbs.common.util.framework.SpringUtil;
import com.tbs.sql.criteria.IQueryCriteria;
import com.tbs.sql.dsl.IQueryBuilder;
import com.tbs.sql.service.IQueryEngine;

/**
 * 框架通用Mapper接口，提供基于查询条件的通用CRUD操作。
 *
 * <p>该接口继承 {@link GenericMapper} 获得单表增删改查能力，并扩展基于
 * {@link IQueryCriteria} 查询条件的查询和统计方法。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>继承 {@link GenericMapper}，提供单表增删改查与批量操作能力</li>
 *   <li>批量插入按方言容量上限自动分批，超出 SQL Server 1000 行或 Oracle IN 1000 项时不会报错</li>
 *   <li>提供基于查询条件的查询方法 {@link #queryByQO}</li>
 *   <li>提供基于查询条件的统计方法 {@link #countByQO}</li>
 *   <li>提供基于实体类型的查询构建器 {@link #query(Class)}</li>
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
 *   <li>实体映射沿用 {@code jakarta.persistence} 注解，与查询引擎共用同一套元数据规则</li>
 *   <li>查询条件需要实现 {@link IQueryCriteria} 接口</li>
 *   <li>分页参数可以为null，表示不分页</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see GenericMapper
 * @see IQueryCriteria
 */
public interface IFrameworkMapper<T, K> extends GenericMapper<T, K>, SmartQueryMapper {

    /**
     * 根据实体类类型获取查询构建器。
     *
     * @param entityClass 实体类类型
     * @return 查询构建器
     */
    default IQueryBuilder<T> query(Class<T> entityClass) {
        return SpringUtil.getBean(IQueryEngine.class).query(entityClass);
    }

    /**
     * 根据实体类对象获取查询构建器。
     *
     * @param entity 实体类对象
     * @return 查询构建器
     */
    default IQueryBuilder<T> query(T entity) {
        return SpringUtil.getBean(IQueryEngine.class).query(entity);
    }


}
