package com.tbs.broadcast.publisher.impl;

import java.io.Serializable;

import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.event.IEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author abstergo
 */
/**
 * 简单分布式接受发布结果类
 * <p>
 * 用于表示简单分布式环境中事件发布的结果，包含事件对象、发布者ID和处理模式
 * </p>
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleDistributedAcceptedPublishResult implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 事件对象
	 */
	private IEvent event;
	/**
	 * 发布者ID
	 */
	private String publisher;
	/**
	 * 处理模式
	 */
	private HandleMode handleMode;
}
