package com.tbs.sql.mapper;


import com.tbs.sql.query.IPage;
import com.tbs.sql.query.IQueryCriteria;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.update.batch.BatchUpdateMapper;
import tk.mybatis.mapper.additional.update.batch.BatchUpdateSelectiveMapper;
import tk.mybatis.mapper.common.BaseMapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

import java.util.List;

/**
 * @author abstergo
 */
public interface IFrameworkMapper<T, K>
    extends BaseMapper<T>, InsertListMapper<T>, BatchUpdateMapper<T>, BatchUpdateSelectiveMapper<T>,
    IdListMapper<T, K> {

    /**
     * 根据查询条件查询
     *
     * @param qo
     * @param page
     * @param entityClass
     * @param <E>
     * @param <Q>
     * @return
     */
    <E, Q extends IQueryCriteria> List<E> queryByQO(Q qo, IPage page, Class<E> entityClass);

    /**
     * 根据查询条件查询数量
     *
     * @param qo
     * @return
     */
    Long countByQO(IQueryCriteria qo);
}
