package com.tbs.shared.interfaces.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.tbs.sql.ef.IEntityQueryWrapper;
import com.tbs.sql.mapper.IFrameworkMapper;

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
	 * 创建一个新的实体对象
	 *
	 * @return
	 */
	protected abstract E newEntity();

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
