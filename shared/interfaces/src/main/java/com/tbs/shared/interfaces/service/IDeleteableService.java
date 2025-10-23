package com.tbs.shared.interfaces.service;

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

    /**
     * 判断实体是否被删除
     *
     * @param entity
     * @return
     */
    boolean isDeleted(E entity);
}
