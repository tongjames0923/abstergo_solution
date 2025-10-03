package com.tbs.app.log;

import com.tbs.common.data.IDataHolderManager;
import com.tbs.common.util.CollUtil;
import com.tbs.lock.util.LockUtil;
import com.tbs.shared.interfaces.log.ISystemOperateLog;
import com.tbs.shared.mapper.SystemOperateMapper;
import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DbSystemOpLog implements ISystemOperateLog, DisposableBean {
	@Resource
	private SystemOperateMapper systemOperateMapper;

	@Resource
	private IDataHolderManager dataHolderManager;

	private static final Logger LOGGER = LoggerFactory.getLogger(DbSystemOpLog.class);

	@Setter
	@Getter
	private int batchSize = 100;

	@Getter
	@Setter
	private String dataHolderName = "DB_SYS_OPER_RECORD_BUFFER";

	@Override
	@Async
	public void log(SystemOperateRecordEntity operateRecord) {
		opDataSource((ls) -> {
			ls.add(operateRecord);
			if (ls.size() >= getBatchSize()) {
				flush(ls);
			}
		});
	}

	private void flush(List<SystemOperateRecordEntity> list) {
		if (list.isEmpty()) {
			return;
		}
		LOGGER.info("flush data in to db.size={},batch size={}", list.size(), getBatchSize());
		for (var ls : CollUtil.partition(list, getBatchSize())) {
			int effect = systemOperateMapper.insertList(ls);
			LOGGER.info("insert list size={},effect size={}", ls.size(), effect);
		}
		list.clear();
	}

	private void opDataSource(Consumer<List<SystemOperateRecordEntity>> consumer) {
		LockUtil.getInstance().lockThen(this, () -> {
			dataHolderManager.getDataHolder(getDataHolderName()).ifPresent(dataHolder -> {
				List<SystemOperateRecordEntity> ls = (List<SystemOperateRecordEntity>) dataHolder
						.get(getDataHolderName()).orElse(new ArrayList<>());
				consumer.accept(ls);
				dataHolder.set(getDataHolderName(), ls, true);
			});
		});
	}

	@Override
	public void destroy() throws Exception {
		LOGGER.warn("destroy DbSystemOPLog... ");
		opDataSource((ls) -> {
			flush(ls);
			ls.clear();
		});
	}
}
