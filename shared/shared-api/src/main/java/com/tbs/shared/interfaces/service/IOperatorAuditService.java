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
    E triggerCreateOperator(E entity);

    /**
     * 触发更新操作
     *
     * @param entity
     * @return
     */
    E triggerUpdateOperator(E entity);

    /**
     * 触发删除操作
     *
     * @param entity
     * @return
     */
    E triggerDeleteOperator(E entity);

}
