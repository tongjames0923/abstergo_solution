package com.tbs.shared.interfaces.service;

import java.util.Date;

/**
 * 审计服务接口
 *
 * @author abstergo
 */
public interface ITimeAuditService<E, K> {

    /**
     * 触发创建审计
     *
     * @param entity
     * @return
     */
    E triggerCreateTime(E entity, Date createTime);

    /**
     * 触发更新审计
     *
     * @param entity
     * @return
     */
    E triggerUpdateTime(E entity, Date updateTime);

    /**
     * 触发删除审计
     *
     * @param entity
     * @return
     */
    E triggerDeleteTime(E entity, Date deleteTime);
}
