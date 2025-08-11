package com.tbs.app.service.log.impl;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import com.tbs.app.mapper.SystemOperateMapper;
import com.tbs.app.pojo.entity.SystemOperateRecordEntity;
import com.tbs.app.service.log.ISystemOperateLog;
import com.tbs.common.util.CollUtil;

import lombok.Getter;
import lombok.Setter;

public class DbSystemOpLog implements ISystemOperateLog, DisposableBean {
	@Resource
	private SystemOperateMapper systemOperateMapper;

	private Queue<SystemOperateRecordEntity> list = new ConcurrentLinkedDeque<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(DbSystemOpLog.class);

	@Setter
	@Getter
	private int batchSize = 100;

	@Override
	public void log(SystemOperateRecordEntity operateRecord) {
		list.add(operateRecord);
		synchronized (this) {
			if (list.size() >= getBatchSize()) {
				flush();
			}
		}
	}

	private void flush() {
		if (list.isEmpty()) {
			return;
		}
		LOGGER.info("flush data in to db.size={},batch size={}", list.size(), getBatchSize());
		synchronized (this) {
			for (var ls : CollUtil.partition(list, getBatchSize())) {
				int effect = systemOperateMapper.insertList(ls);
				LOGGER.info("insert list size={},effect size={}", ls.size(), effect);
			}
			list.clear();
		}
	}

	@Override
	public void destroy() throws Exception {
		flush();
	}
}
