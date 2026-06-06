package com.tbs.shared.interfaces.service;

/**
 * 审计服务接口
 *
 * @param <E>
 * @param <K>
 * @author abstergo
 */
public interface IAuditService<E, K> {

    /**
     * 创建前
     *
     * @param entity
     * @param v
     * @return
     */
    E onCreate(E entity);

    /**
     * 更新前
     *
     * @param entity
     * @param v
     * @return
     */
    E onUpdate(E entity);

    /**
     * 删除前
     *
     * @param entity
     * @param v
     * @return
     */
    E onDelete(E entity);
}
