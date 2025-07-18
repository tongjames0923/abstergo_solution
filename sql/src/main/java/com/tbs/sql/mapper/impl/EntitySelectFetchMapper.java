package com.tbs.sql.mapper.impl;

import com.tbs.sql.model.FetchObject;
import com.tbs.sql.provider.impl.QuerySelectProvider;
import com.tbs.sql.query.IPage;
import com.tbs.sql.query.IQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;


import java.util.List;
import java.util.Map;

/**
 * @author tongj
 */
@Mapper
public interface EntitySelectFetchMapper {
    /**
     * 获取查询对象
     *
     * @param e
     * @return
     */
    @SelectProvider(type = QuerySelectProvider.class, method = "entitySql")
    <T> List<Map<String, Object>> fetch(FetchObject _fetchObject, Map<String, Object> _prs);

    /**
     * 查询计数
     *
     * @param _query
     * @param _compose
     * @param _extractor
     * @param _prs
     * @param <T>
     * @return
     */
    @SelectProvider(type = QuerySelectProvider.class, method = "entityCountSql")
    <T> Long count(FetchObject _fetchObject, Map<String, Object> _prs);

    /**
     * 根据查询对象和分页对象查询列表
     *
     * @param query 查询对象，封装了查询条件
     * @param page  分页对象，用于描述分页信息
     * @return 返回查询结果列表
     *
     *     此方法使用了MyBatis的@SelectProvider注解，指定由QuerySelectProvider类中的dynamicSql方法
     *     生成动态SQL语句，以实现根据查询对象动态查询数据库，并结合分页对象实现分页查询
     */
    @SelectProvider(type = QuerySelectProvider.class, method = "dynamicSql")
    List<Map<String, Object>> queryByQO(IQuery _query, IPage _page, Map<String, Object> _prs);

    /**
     * 根据查询对象统计记录数
     *
     * @param query 查询对象，封装了查询条件
     * @return 返回满足查询条件的记录数
     *
     *     此方法使用了MyBatis的@SelectProvider注解，指定由QuerySelectProvider类中的dynamicCountSql方法
     *     生成动态SQL语句，以实现根据查询对象动态统计数据库中满足条件的记录数
     */
    @SelectProvider(type = QuerySelectProvider.class, method = "dynamicCountSql")
    Long countByQO(IQuery _query, Map<String, Object> _prs);
}
