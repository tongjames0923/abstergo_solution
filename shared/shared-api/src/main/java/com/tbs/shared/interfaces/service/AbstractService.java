package com.tbs.shared.interfaces.service;

import com.tbs.sql.dsl.IEntityQueryWrapper;
import com.tbs.sql.dsl.IQueryBuilder;
import com.tbs.sql.mapper.IFrameworkMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import java.util.Optional;

/**
 * @author abstergo
 */
public abstract class AbstractService<E, K, Mapper extends IFrameworkMapper<E, K>> {

    @Resource
    private IEntityQueryWrapper<E> queryWrapper;
    private E entity;

    /**
     * 获取查询对象
     *
     * @return
     */
    protected IEntityQueryWrapper<E> getQueryWrapper() {
        return queryWrapper;
    }

    /**
     * 获取Mapper对象
     *
     * @return
     */
    protected abstract Mapper getMapper();

    /**
     * 获取查询构建器
     *
     * @return
     */
    protected IQueryBuilder<E> getQueryBuilder() {
        return getMapper().query();
    }


    /**
     * 创建一个新的实体对象
     *
     * @return
     */
    protected abstract E newEntity();

    /**
     * 获取审计服务支持
     *
     * @return
     */
    protected Optional<IAuditService<E, K, ?>> getAuditServiceSupport() {
        return Optional.empty();
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
