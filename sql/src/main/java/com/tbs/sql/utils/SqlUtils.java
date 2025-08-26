package com.tbs.sql.utils;

import java.lang.reflect.Field;

import javax.persistence.Column;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.ReflectUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.sql.annotation.QueryMapping;
import com.tbs.sql.framework.ISqlAssembler;
import com.tbs.sql.framework.driver.IQueryDriver;
import com.tbs.sql.framework.driver.ISqlContext;
import com.tbs.sql.framework.extractor.ISelectSqlDialect;

/**
 * @author tongj
 */
public class SqlUtils {
	/**
	 * 获取字段名
	 *
	 * @param field
	 * @return
	 */
	public static String getFiledNameWithMapping(Field field) {
		Column column = field.getDeclaredAnnotation(Column.class);
		if (column != null && StrUtil.isNotEmpty(column.name())) {
			return column.name();
		}
		QueryMapping queryMapping = field.getDeclaredAnnotation(QueryMapping.class);
		if (queryMapping != null && StrUtil.isNotEmpty(queryMapping.value())) {
			return queryMapping.value();
		}
		return field.getName();
	}

	/**
	 * 是否跳过字段
	 *
	 * @param field
	 * @return
	 */
	public static boolean isSkipField(Field field) {
		return ReflectUtil.isStaticOrFinal(field) || field.isAnnotationPresent(javax.persistence.Transient.class);
	}

	/**
	 * 获取表名
	 *
	 * @param clazz
	 * @return
	 */

	public static String getTableName(Class<?> clazz) {
		if (clazz.isAnnotationPresent(javax.persistence.Table.class)) {
			javax.persistence.Table table = clazz.getAnnotation(javax.persistence.Table.class);
			return table.name();
		}
		return clazz.getSimpleName();
	}


}
