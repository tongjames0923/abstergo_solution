package com.tbs.api_route;

import java.util.Optional;

/**
 * 接口路由条件
 *
 */
public interface IApiRouter {
	/**
	 * 匹配接口路由
	 *
	 * @param apiInfo
	 * @return
	 */
	Optional<IApiRouteExecutor> match(ApiInfo apiInfo);
}
