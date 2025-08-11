package com.tbs.broadcast.event;

import java.io.Serializable;
import java.util.Optional;

/**
 * 广播机制事件接口
 * <p>
 * 该接口定义了广播事件的基本结构和属性，所有需要通过广播机制传递的事件都应实现此接口。
 * 事件包含唯一标识、名称、数据内容以及优先级等属性，支持序列化以便在网络中传输或持久化存储。
 *
 * @author abstergo
 */
public interface IEvent extends Serializable {

	/**
	 * 获取事件唯一标识符
	 * <p>
	 * 每个事件实例都应有全局唯一的ID，用于区分不同的事件实例
	 *
	 * @return 事件唯一标识符字符串
	 */
	String getEventId();

	/**
	 * 获取事件名称
	 * <p>
	 * 事件名称用于标识事件的类型或分类，相同类型的事件应具有相同的名称
	 *
	 * @return 事件名称字符串
	 */
	String getEventName();

	/**
	 * 获取事件数据
	 * <p>
	 * 返回事件携带的具体数据内容，以字节数组形式存储，可以是任何可序列化的对象
	 *
	 * @return 事件数据字节数组
	 */
	byte[] getEventData();

	/**
	 * 设置事件数据
	 * <p>
	 * 将指定的对象序列化为字节数组并设置为事件数据，同时记录数据的原始类型信息
	 *
	 * @param eventData 事件数据字节数组
	 * @param clazz     数据原始类型Class对象，用于后续反序列化时的类型参考
	 */
	void setEventData(byte[] eventData, Class<?> clazz);

	/**
	 * 获取事件数据的类型信息
	 * <p>
	 * 返回事件数据的原始类型Class对象，用于在反序列化时确定正确的类型
	 *
	 * @param clazz 期望的数据类型Class对象
	 * @return 包含事件数据原始类型的Optional对象，如果类型信息不存在则返回空Optional
	 */
	Optional<Class<?>> getEventDataClass();

	/**
	 * 获取事件优先级
	 * <p>
	 * 返回事件的优先级数值，数值越大优先级越高。默认优先级为0
	 *
	 * @return 事件优先级数值
	 */
	default int getEventPriority() {
		return 0;
	}

}
