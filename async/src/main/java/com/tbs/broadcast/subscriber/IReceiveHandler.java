package com.tbs.broadcast.subscriber;

import com.tbs.broadcast.event.IEvent;
import com.tbs.broadcast.publisher.IPublisher;

/**
 * 接收处理接口
 * <p>
 * 该接口定义了事件接收和处理的标准方法。实现该接口的类需要提供具体的事件处理逻辑， 并返回相应的处理结果状态码。处理结果将影响事件的最终状态。
 *
 * @author tongj
 */
public interface IReceiveHandler {
	/**
	 * 接收并处理事件
	 * <p>
	 * 该方法负责处理从发布者接收到的事件，根据具体的业务逻辑执行相应的操作， 并返回处理结果状态码。返回值将决定事件的后续状态变更：
	 * <ul>
	 * <li>1 ({@link AbstractSubscriber#RECEIVE_SUCCESS}) - 处理成功，事件状态将变更为DONE</li>
	 * <li>0 ({@link AbstractSubscriber#RECEIVE_FAILED}) - 处理失败，事件状态将变更为FAILED</li>
	 * <li>-1 ({@link AbstractSubscriber#RECEIVE_RETRY}) -
	 * 需要重试，事件状态将变更为REQUIRE_RETRY</li>
	 * </ul>
	 *
	 * @param event     接收到的事件对象，包含事件的具体数据
	 * @param publisher 事件的发布者，可以通过该对象获取发布者相关信息
	 * @return 事件处理结果状态码： 1表示处理成功， 0表示处理失败， -1表示需要重试
	 * @see AbstractSubscriber
	 */
	int recive(IEvent event, IPublisher publisher);
}
