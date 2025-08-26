package com.tbs.sql.mapper.impl;

import java.util.Map;

public class RawSqlSelectProvider {
	public String dynamicSql(String sql, Map<String, Object> _prs) {
		return sql;
	}

	public String dynamicCountSql(String sql, Map<String, Object> _prs) {
		return "select count(*) from (" + sql + ") as t1";
	}
}
