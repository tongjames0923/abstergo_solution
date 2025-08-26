package com.tbs.sql.framework.intercepter;

import org.springframework.core.Ordered;

import com.tbs.sql.framework.model.SqlWhereCompose;

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
