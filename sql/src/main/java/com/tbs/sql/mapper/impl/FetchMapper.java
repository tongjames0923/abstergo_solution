package com.tbs.sql.mapper.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.sql.framework.driver.IQueryDriver;
import com.tbs.sql.framework.driver.ISqlContext;
import com.tbs.sql.mapper.IRawSqlSelectMapper;
import com.tbs.sql.utils.QueryUtil;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<Map<String, Object>> val =
            mapper.fetch(QueryUtil.getInstance().driveSql(context, driver), context.getSqlParams());
		if (CollUtil.isNotEmpty(val)) {
            List<T> list = new ArrayList<>(val.size());
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
        return mapper.count(QueryUtil.getInstance().driveSql(context, driver), context.getSqlParams());
	}

}
