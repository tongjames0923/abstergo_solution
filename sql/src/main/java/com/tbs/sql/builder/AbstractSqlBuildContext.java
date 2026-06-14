package com.tbs.sql.builder;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL构建上下文抽象实现
 *
 * @author abstergo
 * @since 2.0.0
 */
public abstract class AbstractSqlBuildContext implements SqlBuildContext {
    private Map<String, Object> sqlParams = new HashMap<>(4);
    private boolean valid = true;
    private Object sqlEntity;
    private Class<?> sqlEntityClass;

    @Override
    public Class<?> getEntityClass() { return sqlEntityClass; }

    protected abstract Object provideSqlEntity();

    protected void clearEntity() { sqlEntity = null; sqlEntityClass = null; }

    @Override
    public Object getSqlEntity() { return sqlEntity; }

    @Override
    public Map<String, Object> getSqlParams() { return sqlParams; }

    @Override
    public void destroy() {
        sqlParams.clear();
        sqlEntity = null;
        sqlEntityClass = null;
        valid = false;
    }

    @Override
    public void rebuild() {
        valid = true;
        sqlParams.clear();
        sqlEntity = provideSqlEntity();
        sqlEntityClass = sqlEntity.getClass();
    }

    @Override
    public boolean isValid() { return valid; }
}
