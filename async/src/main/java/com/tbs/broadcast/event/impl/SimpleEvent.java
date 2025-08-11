package com.tbs.broadcast.event.impl;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.SerializationUtils;

import com.tbs.broadcast.event.IEvent;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.StrUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 默认事件实现类
 * <p>
 * 该类是IEvent接口的基本实现，提供了事件的基本属性如事件ID、事件名称、事件数据、
 * 事件优先级等的完整实现。支持事件数据的序列化和反序列化操作，方便事件在网络中传输或持久化存储。
 *
 * @author abstergo
 */
@Slf4j
public class SimpleEvent implements IEvent {
	private static final long serialVersionUID = 1L;

	/**
	 * 事件名称
	 */
	private String eventName;

	/**
	 * 事件数据（字节数组形式）
	 */
	private byte[] eventData;

	/**
	 * 事件数据的类名（用于反序列化时确定数据类型）
	 */
	private String eventDataClass;

	/**
	 * 事件优先级，默认为0
	 */
	private int eventPriority = 0;

	/**
	 * 事件唯一标识符
	 */
	private String eventId;

	@Override
	public String getEventId() {
		return eventId;
	}

	@Override
	public String getEventName() {
		return eventName;
	}

	/**
	 * 构造函数
	 *
	 * @param eventId   事件唯一标识符，不能为null
	 * @param eventName 事件名称，不能为null
	 */
	public SimpleEvent(String eventId, String eventName) {
		this.eventName = eventName;
		this.eventId = eventId;
	}

	/**
	 * 默认构造函数
	 * <p>
	 * 创建一个默认的事件实例，事件ID和事件名称都设置为"default"
	 */
	public SimpleEvent() {
		this.eventName = "default";
		this.eventId = "default";
	}

	@Override
	public byte[] getEventData() {
		return eventData;
	}

	@Override
	public void setEventData(byte[] eventData, Class<?> clazz) {
		if (eventData == null) {
			this.eventDataClass = null;
			this.eventData = null;
			return;
		}
		AssertUtil.notNull(eventData, "eventData can not be null");
		AssertUtil.notNull(clazz, "class can not be null");
		this.eventData = eventData;
		this.eventDataClass = clazz.getName();
	}

	/**
	 * 设置可序列化的事件数据
	 * <p>
	 * 将指定的对象序列化为字节数组并设置为事件数据，同时记录数据的原始类型信息
	 *
	 * @param data 要设置的事件数据对象，不能为null
	 * @param <T>  事件数据的类型
	 */
	public <T> void setDataForSerialization(@NotNull T data) {
		setEventData(SerializationUtils.serialize(data), data.getClass());
	}

	/**
	 * 获取反序列化的事件数据
	 * <p>
	 * 将字节数组形式的事件数据反序列化为原始对象并返回
	 *
	 * @return 反序列化后的事件数据对象
	 */
	public Object getDeserializedData() {
		return SerializationUtils.deserialize(eventData);
	}

	@Override
	public Optional<Class<?>> getEventDataClass() {
		if (StrUtil.isEmpty(eventDataClass)) {
			return Optional.empty();
		}
		try {
			return Optional.ofNullable(Class.forName(eventDataClass));
		} catch (ClassNotFoundException e) {
			log.error("class not found", e);
			return Optional.empty();
		}
	}

	@Override
	public int getEventPriority() {
		return eventPriority;
	}
}
