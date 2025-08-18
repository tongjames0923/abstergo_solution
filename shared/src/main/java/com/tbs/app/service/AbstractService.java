package com.tbs.app.service;

import com.tbs.sql.ef.IEntityQueryWrapper;
import com.tbs.sql.mapper.IFrameworkMapper;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author abstergo
 */
public abstract class AbstractService<E, K, Mapper extends IFrameworkMapper<E, K>> {

    @Getter
    @Resource
    protected IEntityQueryWrapper<E> queryWrapper;
    private E entity;

    /**
     * 获取Mapper对象
     *
     * @return
     */
    protected abstract Mapper getMapper();

    /**
     * 创建一个新的实体对象
     *
     * @return
     */
    protected abstract E newEntity();

    /**
     * 获取实体对象类型
     *
     * @return
     */
    protected Class<E> getEntityClass() {
        return (Class<E>)entity.getClass();
    }

    /**
     * 获取实体对象模板类
     *
     * @return
     */
    protected E getEntity() {
        return entity;
    }

    @PostConstruct
    void init() {
        entity = newEntity();
        queryWrapper.makeQueryable(entity);
    }

}
