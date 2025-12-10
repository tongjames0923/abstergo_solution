package com.tbs.common;

import java.io.Serializable;

/**
 * 枚举基类接口
 * <p>
 * 该接口定义了枚举类型的通用结构，所有需要编码和描述信息的枚举都应该实现此接口。 接口提供了获取枚举编码和枚举描述值的标准方法，便于统一处理各种枚举类型。
 * 实现该接口的枚举类型必须是可序列化的。
 *</p>
 *
 * @param <T> 枚举详情值的类型
 * @author tongj
 * @since 1.0.0
 */
public interface BaseEnum<T> extends Serializable {

	/**
	 * 获取枚举详情值
	 * <p>
	 * 返回枚举项的详细描述信息，通常是一个具有业务含义的字符串， 用于在界面展示或日志记录中提供更友好的信息。
	 *
	 * @return 枚举详情值
	 */
	public T getValue();

	/**
	 * 获取枚举编码
	 * <p>
	 * 返回枚举项的整数编码，通常用于数据库存储或系统内部处理， 编码值应该是唯一的，便于程序识别和处理不同的枚举项。
	 *
	 * @return 枚举编码值
	 */
	public Integer getCode();

}
