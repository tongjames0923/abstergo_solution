package com.tbs.app.service;

import com.tbs.sql.ef.IEntityQueryWrapper;
import com.tbs.sql.mapper.IFrameworkMapper;
import lombok.Getter;

import javax.annotation.Resource;

/**
 * @author abstergo
 */
public abstract class AbstractService<E, Mapper extends IFrameworkMapper<E, Long>> {
    @Getter
    @Resource
    protected Mapper mapper;

    @Resource
    protected IEntityQueryWrapper<E> queryWrapper;

    private E entity;

    /**
     * 创建一个新的实体对象
     *
     * @return
     */
    protected abstract E newEntity();

    public IEntityQueryWrapper<E> getQueryWrapper() {
        if (entity == null) {
            entity = newEntity();
            queryWrapper.makeQueryable(entity);
        }
        return queryWrapper;
    }
}
