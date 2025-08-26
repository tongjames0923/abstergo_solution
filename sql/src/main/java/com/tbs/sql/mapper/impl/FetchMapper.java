package com.tbs.sql.mapper.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.tbs.common.util.BeanUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.sql.framework.driver.IQueryDriver;
import com.tbs.sql.framework.driver.ISqlContext;
import com.tbs.sql.mapper.IRawSqlSelectMapper;
import com.tbs.sql.utils.QueryUtil;

import cn.hutool.core.bean.copier.CopyOptions;

/**
 * @author abstergo
 */
public class FetchMapper {
	@Resource
	private IRawSqlSelectMapper mapper;

	/**
	 * 查询
	 *
	 * @param _query
	 * @param _page
	 * @param _compose
	 * @param _extractor
	 * @param _orderInfos
	 * @param <T>
	 * @return *
	 */
	public <T> List<T> fetch(ISqlContext context, IQueryDriver driver, Class<T> tClass) {
		List<Map<String, Object>> val = mapper.fetch(QueryUtil.driveSql(context, driver), context.getSqlParams());
		if (CollUtil.isNotEmpty(val)) {
			List<T> list = new LinkedList<>();
			for (Map<String, Object> map : val) {
				list.add(BeanUtil.<T>mapToBean(map, tClass, true, CopyOptions.create()));
			}
			return list;
		}
		return new ArrayList<>();
	}

	/**
	 * 查询单个结果
	 *
	 * @param query
	 * @param _compose
	 * @param _extractor
	 * @param <T>
	 * @return
	 */

	public <T> Long count(ISqlContext context, IQueryDriver driver) {
		return mapper.count(QueryUtil.driveSql(context, driver), context.getSqlParams());
	}

}
