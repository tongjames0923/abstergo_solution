package com.tbs.sql.ef;

import java.util.List;

/**
 * 查询结果获取接口，用于执行查询并获取结果
 * <p>
 * 该接口提供了获取查询结果的核心方法，包括获取所有结果、获取单个结果和统计结果数量等操作。
 * 作为查询执行的最终环节，负责将SQL查询转换为实际的数据结果。
 * </p>
 *
 * @param <T> 实体类类型，表示查询结果的数据类型
 * @author abstergo
 */
public interface IQueryFetcher<T> {

    /**
     * 获取查询结果列表
     * <p>
     * 执行查询操作并返回所有符合条件的结果列表。
     * 如果没有符合条件的记录，则返回空列表。
     * </p>
     *
     * @return 查询结果列表，包含所有符合条件的实体对象
     * @see List
     */
    List<T> fetch();

    /**
     * 获取查询第一个结果
     * <p>
     * 执行查询操作并返回第一条符合条件的结果。
     * 如果没有符合条件的记录，则返回null。
     * 通常用于只需要获取单个结果的场景，如根据主键查询单条记录。
     * </p>
     *
     * @return 查询结果中的第一个实体对象，如果无结果则返回null
     */
    T fetchOne();

    /**
     * 获取查询结果数量
     * <p>
     * 执行COUNT查询操作，返回符合条件的记录总数。
     * 生成类似 SELECT COUNT(*) FROM table WHERE ... 的SQL语句。
     * </p>
     *
     * @return 符合条件的记录总数
     */
    Long count();

}
