package com.tbs.sql.builder;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL构建上下文接口
 *
 * @author abstergo
 * @since 2.0.0
 */
public interface SqlBuildContext {
    Class<?> getEntityClass();
    Object getSqlEntity();
    Map<String, Object> getSqlParams();
    boolean isValid();
    void destroy();
    void rebuild();
}
