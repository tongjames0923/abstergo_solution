package com.tbs.sql.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;

import com.tbs.sql.mapper.impl.RawSqlSelectProvider;

@Mapper
public interface IRawSqlSelectMapper {
	@SelectProvider(type = RawSqlSelectProvider.class, method = "dynamicSql")
	List<Map<String, Object>> fetch(String sql, Map<String, Object> _prs);

	@SelectProvider(type = RawSqlSelectProvider.class, method = "dynamicCountSql")
	Long count(String sql, Map<String, Object> _prs);

}
