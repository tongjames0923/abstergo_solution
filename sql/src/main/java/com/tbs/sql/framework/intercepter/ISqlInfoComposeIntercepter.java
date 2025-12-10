package com.tbs.sql.framework.intercepter;

import org.springframework.core.Ordered;

import com.tbs.sql.framework.model.SqlWhereCompose;

/**
 * SQL信息组合拦截器接口
 * <p>
 * 定义SQL信息组合拦截的方法，用于拦截和处理SQL信息组合
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ISqlInfoComposeIntercepter extends Ordered {
	/**
	 * 拦截SQL信息组合
	 * <p>
	 * 拦截SQL条件组合信息，对SQL条件进行处理
	 * </p>
	 *
	 * @param compose SQL条件组合信息
	 * @param entityClass SQL实体类
	 */
	void intercept(SqlWhereCompose compose, Class<?> entityClass);

	@Override
	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
