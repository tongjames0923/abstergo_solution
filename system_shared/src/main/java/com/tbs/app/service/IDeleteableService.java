package com.tbs.app.service;

import java.util.Collection;

/**
 * 删除服务接口
 *
 * @author abstergo
 */
public interface IDeleteableService<E, K> {
    /**
     * 根据id删除
     *
     * @param id
     * @return
     */
    E deleteById(K id);

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    int deleteByIds(Collection<K> ids);
}
