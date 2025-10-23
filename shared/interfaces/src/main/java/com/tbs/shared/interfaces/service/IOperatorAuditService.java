package com.tbs.shared.interfaces.service;

/**
 * 操作人审计服务接口
 *
 * @author abstergo
 */
public interface IOperatorAuditService<E> {

    /**
     * 触发创建操作
     *
     * @param entity
     * @return
     */
    E triggerCreate(E entity, String byUserUniqueName);

    /**
     * 触发更新操作
     *
     * @param entity
     * @return
     */
    E triggerUpdate(E entity, String byUserUniqueName);

    /**
     * 触发删除操作
     *
     * @param entity
     * @return
     */
    E triggerDelete(E entity, String byUserUniqueName);

}
