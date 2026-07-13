package com.tbs.shared.interfaces.service;

import com.tbs.shared.pojo.*;

import java.util.Date;

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
