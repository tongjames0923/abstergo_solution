package com.tbs.logging.impl;

import com.tbs.broadcast.event.impl.SimpleEvent;
import com.tbs.broadcast.publisher.IPublisher;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.interfaces.log.ISystemOperateLog;
import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;

import javax.annotation.Resource;

/**
 * 基于事件系统的系统操作日志实现类
 *
 * @author tongj
 */
public class EventSystemOpLog implements ISystemOperateLog {

	@Resource
	private IPublisher publisher;

	public static final String DEFAULT_EVENT_ID = "system_operate_log";

	/**
	 * 事件ID
	 */
	private String eventId;

	/**
	 * 是否异步
	 */
	private boolean isAsync;

	public EventSystemOpLog(String eventId, boolean isAsync) {
		this.eventId = StrUtil.isEmpty(eventId) ? DEFAULT_EVENT_ID : eventId;
		this.isAsync = isAsync;
	}

	public EventSystemOpLog() {
		this(null, true);
	}

	@Override
	public void log(SystemOperateRecordEntity operateRecord) {
		SimpleEvent event = new SimpleEvent(eventId, operateRecord.getOperateType());
		event.setDataForSerialization(operateRecord);
		if (isAsync) {
			publisher.pushlishAsync(event);
		} else {
			publisher.pushlishSync(event);
		}
	}
}
