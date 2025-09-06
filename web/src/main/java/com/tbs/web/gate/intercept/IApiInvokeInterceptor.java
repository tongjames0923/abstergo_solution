package com.tbs.web.gate.intercept;

import java.lang.reflect.Method;

import com.tbs.common.data.ValueWrapper;
import com.tbs.web.BaseController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API调用拦截器接口
 * <p>
 * 该接口定义了API方法调用过程中的拦截点，允许在方法调用前后以及异常发生时执行自定义逻辑。
 * 通过实现该接口，可以实现诸如日志记录、性能监控、权限检查、参数验证等横切关注点功能，
 * 而无需修改原有业务代码，符合面向切面编程(AOP)的思想。提供了统一的上下文管理和拦截方法定义。
 *
 * @author abstergo
 * @see Method Java反射方法类
 * @see ValueWrapper 值包装类
 * @see BaseController 基础控制器类
 * @see ApiInvokeContext API调用上下文内部类
 */
public interface IApiInvokeInterceptor {

	/**
	 * API调用上下文类
	 * <p>
	 * 用于在拦截器各方法间传递调用相关信息的上下文对象，包含目标控制器、调用方法和参数等核心信息。
	 * 通过Lombok注解简化getter/setter/构造函数的编写，便于在拦截器链中传递和使用。
	 *
	 * @author abstergo
	 * @see BaseController 基础控制器类
	 * @see Method Java反射方法类
	 * @see Data Lombok数据注解
	 * @see AllArgsConstructor Lombok全参构造注解
	 * @see NoArgsConstructor Lombok无参构造注解
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ApiInvokeContext {
		/**
		 * 目标控制器对象
		 * <p>
		 * 被调用方法所属的控制器实例，通常是BaseController的子类实例。
		 *
		 * @see BaseController 基础控制器类
		 */
		private BaseController target;

		/**
		 * 被调用的方法对象
		 * <p>
		 * 通过Java反射获取的方法对象，包含方法名、参数类型、返回类型等信息。
		 *
		 * @see Method Java反射方法类
		 */
		private Method method;

		/**
		 * 方法调用参数数组
		 * <p>
		 * 调用目标方法时传入的参数值数组，可能为null或空数组。
		 */
		private Object[] args;
	}

	/**
	 * 方法调用前拦截
	 * <p>
	 * 在目标方法被调用之前执行，可以用于参数验证、权限检查、日志记录、性能统计等操作。 此时方法尚未执行，可以在此处修改上下文信息或阻止方法执行。
	 *
	 * @param context API调用上下文，包含目标对象、方法和参数等信息，可能为null
	 * @return 更新后的API调用上下文，可以是原上下文或新建的上下文对象
	 * @see ApiInvokeContext API调用上下文类
	 */
	ApiInvokeContext beforeInvoke(ApiInvokeContext context);

	/**
	 * 方法调用后拦截
	 * <p>
	 * 在目标方法成功执行完成后调用，可以用于结果处理、日志记录、性能统计等操作。 此时方法已执行完毕并返回了结果，可以在此处修改返回结果。
	 *
	 * @param context API调用上下文，包含目标对象、方法和参数等信息，可能为null
	 * @param result  方法执行后的返回结果包装器，不能为null
	 * @see ApiInvokeContext API调用上下文类
	 * @see ValueWrapper 值包装类
	 */
	void afterInvoke(ApiInvokeContext context, ValueWrapper<Object> result);

	/**
	 * 异常处理拦截
	 * <p>
	 * 在目标方法执行过程中发生异常时调用，可以用于异常日志记录、异常转换、 故障恢复等操作。此方法在异常抛出前调用，可以在此处处理或转换异常。
	 *
	 * @param context API调用上下文，包含目标对象、方法和参数等信息，可能为null
	 * @param e       方法执行过程中抛出的异常，不能为null
	 * @see ApiInvokeContext API调用上下文类
	 * @see Throwable 异常基类
	 */
	void onException(ApiInvokeContext context, Throwable e);
}
