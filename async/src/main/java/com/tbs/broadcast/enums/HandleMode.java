package com.tbs.broadcast.enums;

import com.tbs.common.BaseEnum;

/**
 * 处理模式枚举类
 * <p>
 * 定义了事件处理的两种模式：同步处理和异步处理。 同步处理会在当前线程中立即执行事件处理逻辑， 异步处理会将事件放入队列中，由其他线程处理。
 *
 * @author abstergo
 */
public enum HandleMode implements BaseEnum<String> {
	/**
	 * 同步处理模式
	 * <p>
	 * 事件发布后会在当前线程中立即处理，阻塞直到处理完成
	 */
	SYNC("同步", 0),

	/**
	 * 异步处理模式
	 * <p>
	 * 事件发布后会被放入处理队列中，由专门的处理线程异步处理，不阻塞当前线程
	 */
	ASYNC("异步", 1);

	/**
	 * 处理模式的描述文本
	 */
	private String value;

	/**
	 * 处理模式的编码
	 */
	private int code;

	/**
	 * 构造函数
	 *
	 * @param value 处理模式的描述文本
	 * @param code  处理模式的编码
	 */
	HandleMode(String value, int code) {
		this.value = value;
		this.code = code;
	}

	/**
	 * 获取处理模式的描述文本
	 *
	 * @return 处理模式的描述文本
	 */
	@Override
	public String getValue() {
		return value;
	}

	/**
	 * 获取处理模式的编码
	 *
	 * @return 处理模式的编码
	 */
	@Override
	public Integer getCode() {
		return code;
	}
}
