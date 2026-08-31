package com.tbs.shared.interfaces.service;

import com.tbs.common.data.PageVO;
import com.tbs.sql.criteria.IPage;
import com.tbs.sql.criteria.IQueryCriteria;

import java.util.List;

/**
 * 搜索可提供搜索的服务
 *
 * @author abstergo
 */
public interface ISearchableService<E, K> {
    /**
     * 查询
     *
     * @param qo   查询条件
     * @param page 分页参数
     * @return
     */
    PageVO<E> queryAndPage(IQueryCriteria qo, IPage page);

    /**
     * 查询
     *
     * @param qo   查询条件
     * @param page 分页参数
     * @return
     */
    List<E> query(IQueryCriteria qo, IPage page);

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    E queryById(K id);

}
