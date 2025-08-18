package com.tbs.app.service;

import com.tbs.app.pojo.model.PageVO;
import com.tbs.sql.query.IPage;
import com.tbs.sql.query.IQueryCriteria;

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
    PageVO<E> queryBy(IQueryCriteria qo, IPage page);

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    E queryById(K id);

}
