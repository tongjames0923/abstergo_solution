package com.tbs.sql.ef.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.sql.ef.IEntityQueryWrapper;
import com.tbs.sql.ef.IQueryQueryBuilder;

/**
 * @author tongj
 */
public class SimpleEntityQueryWrapper<T> implements IEntityQueryWrapper<T> {
    private T object;

    @Override
    public IEntityQueryWrapper<T> makeQueryable(T e) {
        AssertUtil.notNull(e, "可查询化失败，请传入对象");
        object = e;
        return this;
    }

    @Override
    public IQueryQueryBuilder<T> query() {
        AssertUtil.notNull(object, "请先调用makeQueryable方法");
        return new EntityQueryBuilder<T>(object);
    }
}
