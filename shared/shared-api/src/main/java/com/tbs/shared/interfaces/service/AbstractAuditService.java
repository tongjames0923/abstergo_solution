package com.tbs.shared.interfaces.service;

import com.tbs.shared.pojo.*;

import java.util.Date;

/**
 * 审计服务抽象类
 *
 * @param <E>  实体类
 * @param <K>  主键类型
 * @param <UK> 用户ID类型
 *             <p>
 *             用于创建、更新、删除操作的审计，在服务模板中使用
 *             </p>
 * @see AbstractFullFeatureServiceTemplate
 * @author tongj
 */
public abstract class AbstractAuditService<E, K, UK> implements IAuditService<E, K, UK> {

    @Override
    public E onCreate(E entity) {
        if (entity instanceof ICreateTimeAuditable) {
            ((ICreateTimeAuditable)entity).setCreateTime(new Date());
        }
        if (entity instanceof ICommonCreateAuditable) {
            ((ICommonCreateAuditable)entity).setCreateBy(getCurrentUserId());
        }

        return entity;
    }

    @Override
    public E onUpdate(E entity) {
        if (entity instanceof IUpdateTimeAuditable) {
            ((IUpdateTimeAuditable)entity).setUpdateTime(new Date());
        }
        if (entity instanceof ICommonUpdateAuditable) {
            ((ICommonUpdateAuditable<UK>)entity).setUpdateBy(getCurrentUserId());
        }

        return entity;
    }

    @Override
    public E onDelete(E entity) {
        if (entity instanceof ICommonDeleteAuditable) {
            ((ICommonDeleteAuditable)entity).setDeleteTime(new Date());
        }
        if (entity instanceof ICommonDeleteAuditable) {
            ((ICommonDeleteAuditable)entity).setDeleteBy(getCurrentUserId());
        }

        return entity;
    }
}
