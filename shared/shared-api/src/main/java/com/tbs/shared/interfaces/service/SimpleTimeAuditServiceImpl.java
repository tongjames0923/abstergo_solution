package com.tbs.shared.interfaces.service;

import com.tbs.shared.pojo.ICommonTimeAuditable;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author abstergo
 */
@Slf4j
public class SimpleTimeAuditServiceImpl<E extends ICommonTimeAuditable> implements ITimeAuditService<E, Long> {
    @Override
    public E onCreate(E entity) {
        if (entity == null) {
            return null;
        }
        entity.setCreateTime(new Date());
        return entity;
    }

    @Override
    public E onUpdate(E entity) {
        if (entity == null) {
            return null;
        }
        entity.setUpdateTime(new Date());
        return entity;
    }

    @Override
    public E onDelete(E entity) {
        if (entity == null) {
            return null;
        }
        log.debug("delete entity: {}", entity);
        return entity;
    }
}
