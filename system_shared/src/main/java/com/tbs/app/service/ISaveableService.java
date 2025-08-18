package com.tbs.app.service;

import java.util.List;

/**
 * 保存服务接口
 *
 * @author abstergo
 */
public interface ISaveableService<E, K> {
    /**
     * 保存
     *
     * @param entity
     * @return
     */
    List<E> save(E entity);

}
