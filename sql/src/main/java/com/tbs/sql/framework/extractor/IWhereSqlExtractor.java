package com.tbs.sql.framework.extractor;

import java.util.List;
import java.util.Map;

import com.tbs.sql.framework.converter.IValueConverter;
import com.tbs.sql.framework.model.SqlWhereCompose;

/**
 * where语句提取器
 *
 * @author abstergo
 */
public interface IWhereSqlExtractor {
	/**
	 * 获取查询条件SQL
	 *
	 * @param query
	 * @param params
	 * @param sqlWhereCompose
	 * @return
	 */
	public String extraWhereSql(SqlWhereCompose sqlWhereCompose, Map<String, Object> params);

	/**
	 * 获取值转换器
	 *
	 * @return
	 */
	public List<IValueConverter> getValueConverters();

}
