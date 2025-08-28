package com.tbs.api_route.aop;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.tbs.api_route.ApiInfo;
import com.tbs.api_route.IApiRouter;
import com.tbs.api_route.annotation.ApiRoute;
import com.tbs.common.util.SpringUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class ApiRouteAop {

	private static Map<Method, IApiRouter> apiRouteMap = new ConcurrentHashMap<>(16);

	@Around("@annotation(com.tbs.api_route.annotation.ApiRoute)")
	public Object aroundApiRoute(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		IApiRouter apiRouter = apiRouteMap.computeIfAbsent(signature.getMethod(), this::getApiRouter);
		if (apiRouter == null) {
			return joinPoint.proceed();
		}
		var apiInfo = new ApiInfo(joinPoint.getTarget(), signature.getMethod(), joinPoint.getArgs());
		var executor = apiRouter.match(apiInfo);
		if (executor.isPresent()) {
			return executor.get().execute(apiInfo);
		}
		return joinPoint.proceed();

	}

	private IApiRouter getApiRouter(Method method) {
		try {
			ApiRoute routeAnnotation = method.getDeclaredAnnotation(ApiRoute.class);
			if (routeAnnotation == null) {
				return null;
			}
			return SpringUtil.getBean(routeAnnotation.value());
		} catch (Throwable e) {
			log.error("无法获取到Method:{}的Api路由信息", method, e);
		}
		return null;
	}

}
