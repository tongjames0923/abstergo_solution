package com.tbs.sql.interceptor;

import com.tbs.sql.model.where.WhereCompose;
import org.springframework.core.Ordered;

/**
 * SQL WHERE条件组合拦截器接口
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface SqlComposeInterceptor extends Ordered {
    WhereCompose intercept(WhereCompose compose, Class<?> entityClass);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
