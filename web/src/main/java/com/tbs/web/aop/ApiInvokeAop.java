package com.tbs.web.aop;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.tbs.common.IStartup;
import com.tbs.common.data.ValueWrapper;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.BaseController;
import com.tbs.web.gate.intercept.IApiInvokeInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * API调用切面实现类
 * <p>
 * 该类基于Spring AOP实现，用于拦截所有继承自BaseController的控制器中带有HTTP映射注解的方法调用，
 * 并在方法调用前后以及异常发生时执行相应的拦截器逻辑。通过切面编程实现对API调用的统一管理和监控，
 * 支持日志记录、性能监控、权限检查等横切关注点功能。同时实现了IStartup接口，在系统启动时初始化拦截器列表。
 *
 * @author abstergo
 * @see IApiInvokeInterceptor API调用拦截器接口
 * @see IStartup 系统启动接口
 * @see Aspect 切面注解
 * @see Component 组件注解
 * @see Slf4j 日志注解
 */
@Aspect
@Component
@Slf4j
public class ApiInvokeAop implements IStartup {

	/**
	 * 切点定义
	 * <p>
	 * 定义需要被拦截的方法切点，包括所有继承自BaseController的控制器类中， 带有以下HTTP映射注解的方法：
	 * <ul>
	 * <li>@RequestMapping</li>
	 * <li>@GetMapping</li>
	 * <li>@PutMapping</li>
	 * <li>@PostMapping</li>
	 * <li>@DeleteMapping</li>
	 * <li>@PatchMapping</li>
	 * </ul>
	 *
	 * @see Pointcut 切点注解
	 */
	@Pointcut("within(com.tbs.web.BaseController+) && (@annotation(org.springframework.web.bind.annotation.RequestMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.GetMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.PutMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.PostMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.DeleteMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.PatchMapping))")
	public void pointcut() {
	}

	/**
	 * 拦截器列表
	 * <p>
	 * 存储所有已注册的API调用拦截器实例，用于在方法调用前后以及异常发生时执行相应的拦截逻辑。
	 * 初始容量为8，通过startUp方法在系统启动时从Spring容器中获取所有IApiInvokeInterceptor实现类实例进行初始化。
	 *
	 * @see IApiInvokeInterceptor API调用拦截器接口
	 * @see ArrayList 动态数组实现
	 */
	List<IApiInvokeInterceptor> interceptors = new ArrayList<>(8);

	/**
	 * 环绕通知方法
	 * <p>
	 * 实现对目标方法的环绕拦截，在方法调用前后以及异常发生时执行相应的拦截器逻辑：
	 * <ol>
	 * <li>调用所有拦截器的beforeInvoke方法</li>
	 * <li>执行目标方法</li>
	 * <li>如果方法执行成功，调用所有拦截器的afterInvoke方法</li>
	 * <li>如果方法执行异常，调用所有拦截器的onException方法</li>
	 * </ol>
	 *
	 * @param joinPoint 连接点对象，包含目标方法的相关信息
	 * @return 目标方法的执行结果
	 * @throws Throwable 方法执行过程中可能抛出的异常
	 * @see Around 环绕通知注解
	 * @see ProceedingJoinPoint 连接点接口
	 * @see MethodSignature 方法签名接口
	 * @see IApiInvokeInterceptor API调用拦截器接口
	 * @see ValueWrapper 值包装类
	 * @see AssertUtil 断言工具类
	 */
	@Around("pointcut()")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		List<IApiInvokeInterceptor.ApiInvokeContext> contexts = new ArrayList<>(interceptors.size());
		for (IApiInvokeInterceptor interceptor : interceptors) {
			IApiInvokeInterceptor.ApiInvokeContext context = new IApiInvokeInterceptor.ApiInvokeContext(
					(BaseController) joinPoint.getTarget(), methodSignature.getMethod(), joinPoint.getArgs());
			contexts.add(interceptor.beforeInvoke(context));
		}
		ValueWrapper<Object> result = new ValueWrapper<>();
		try {
			result.set(joinPoint.proceed());
		} catch (RuntimeException e) {
			for (int i = 0; i < contexts.size(); i++) {
				interceptors.get(i).onException(contexts.get(i), e);
			}
			AssertUtil.throwError(() -> e);
		} catch (Throwable e) {
			for (int i = 0; i < contexts.size(); i++) {
				interceptors.get(i).onException(contexts.get(i), e);
			}
			throw e;
		}
		for (int i = 0; i < contexts.size(); i++) {
			interceptors.get(i).afterInvoke(contexts.get(i), result);
		}
		return result.get().orElse(null);
	}

	/**
	 * 系统启动初始化方法
	 * <p>
	 * 实现IStartup接口的方法，在系统启动时自动调用。从Spring容器中获取所有IApiInvokeInterceptor实现类实例，
	 * 并添加到拦截器列表中，完成拦截器的初始化工作。如果初始化过程中发生异常，将记录错误日志但不会中断系统启动流程。
	 *
	 * @throws RuntimeException 初始化过程中可能抛出的运行时异常
	 * @see IStartup#startUp() 系统启动接口方法
	 * @see SpringUtil Spring工具类
	 * @see IApiInvokeInterceptor API调用拦截器接口
	 */
	@Override
	public void startUp() throws RuntimeException {
		try {
			SpringUtil.getBeansOfType(IApiInvokeInterceptor.class).values().stream().forEach((i) -> {
				interceptors.add(i);
			});
			log.info("接口访问器初始化完成，使用{}个拦截器", interceptors.size());
		} catch (Throwable e) {
			log.error("初始化接口访问器异常", e);
		}
	}
}
