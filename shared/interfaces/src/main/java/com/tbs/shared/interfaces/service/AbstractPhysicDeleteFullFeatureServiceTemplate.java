package com.tbs.shared.interfaces.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.tbs.common.util.CollUtil;
import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.sql.mapper.IFrameworkMapper;

/**
 * 物理删除全功能服务模板
 *
 * @author tongj
 */
public abstract class AbstractPhysicDeleteFullFeatureServiceTemplate<T extends IIdentifiable<K>, K, M extends IFrameworkMapper<T, K>>
		extends AbstractFullFeatureServiceTemplate<T, K, M> {

	private static final int BATCH_UPDATE_SIZE = 30;

	/**
	 * 获取禁用的标志
	 *
	 * @return
	 */
	protected abstract Integer getFlagDisable();

	@Transactional(rollbackFor = Exception.class)
	@Override
	protected void handleDelete(List<T> entitys) {
		List<K> disableEntitys = entitys.stream().map(entity -> {
			return entity.getId();
		}).collect(Collectors.toList());
		if (disableEntitys.size() <= BATCH_UPDATE_SIZE) {
			getMapper().deleteByIdList(disableEntitys);
		} else {
			for (List<K> batch : CollUtil.partition(disableEntitys, BATCH_UPDATE_SIZE)) {
				getMapper().deleteByIdList(batch);
			}
		}
	}
}
