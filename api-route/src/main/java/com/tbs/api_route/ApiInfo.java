package com.tbs.api_route;

import java.lang.reflect.Method;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiInfo {
	/**
	 * 源对象
	 */
	private Object source;
	/**
	 * 方法
	 */
	private Method method;
	/**
	 * 参数传入的实际参数
	 */
	private Object[] args;

}
