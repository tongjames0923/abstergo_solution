package com.tbs.sql;

import com.tbs.sql.model.SqlWhereCompose;
import org.springframework.core.Ordered;

/**
 * sql信息组合拦截器
 *
 * @author abstergo
 */
public interface ISqlInfoComposeIntercepter extends Ordered {
    /**
     * 拦截sql信息组合
     *
     * @param compose
     */
    void intercept(SqlWhereCompose compose, Class<?> entityClass);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
