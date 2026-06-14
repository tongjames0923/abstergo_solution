package com.tbs.sql.mapper.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.sql.annotation.OrField;
import com.tbs.sql.annotation.QueryField;
import com.tbs.sql.enums.QueryConnectorEnum;
import com.tbs.sql.framework.driver.impl.ImportedSqlContext;
import com.tbs.sql.framework.model.SqlWhereCompose;
import com.tbs.sql.framework.model.SqlWhereInfo;
import com.tbs.sql.framework.model.SqlWhereNode;
import com.tbs.sql.provider.ICustomWhereProvider;
import com.tbs.sql.query.IQueryCriteria;
import com.tbs.sql.utils.SqlUtils;
import lombok.Getter;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FrameworkSelectProvider<T> {
	private static final Map<Class<?>, Object> sqlEntitySupplierMap = new ConcurrentHashMap<>();

	/**
	 * 表字段元数据类，用于封装字段的元数据信息
	 */
	private static class FieldMetadata {
		/**
		 * 字段对象
		 */
		@Getter
		private final Field field;

		/**
		 * 查询字段集合，用于表示该字段可以进行哪些查询操作
		 */
		private final Set<QueryField> queryFields;

		/**
		 * 查询连接器枚举，定义了字段之间的查询关系
		 */
		@Getter
		private final QueryConnectorEnum connectorEnum;

		private final List<QueryField> queryFieldList;

		/**
		 * 构造方法，初始化字段元数据对象
		 *
		 * @param field         字段对象
		 * @param queryFields   查询字段集合
		 * @param connectorEnum 查询连接器枚举
		 */
		public FieldMetadata(Field field, Set<QueryField> queryFields, QueryConnectorEnum connectorEnum) {
			this.field = field;
			this.queryFields = queryFields;
			this.connectorEnum = connectorEnum;
			// 预先排序以避免每次使用时重复排序
			queryFieldList = queryFields.stream().sorted(Comparator.comparingInt(QueryField::index))
					.collect(Collectors.toList());
		}

		/**
		 * 获取查询字段集合
		 *
		 * @return 查询字段集合
		 */
		public List<QueryField> getQueryFields() {
			return queryFieldList;
		}

	}

	// ==================== 缓存 ====================
	// 缓存Map，用于存储每个类的字段信息，指定初始容量
	private static final Map<Class<?>, List<FieldMetadata>> fieldMetadataCache = new ConcurrentHashMap<>(128);

	/**
	 * 获取字段元数据
	 *
	 * @param clazz 类对象
	 * @return 字段元数据列表
	 */
	private static List<FieldMetadata> getFieldMetadata(Class<?> clazz) {
		return fieldMetadataCache.computeIfAbsent(clazz, FrameworkSelectProvider::buildFieldMetadata);
	}

	/**
	 * 构建字段元数据
	 *
	 * @param clazz 类对象
	 * @return 字段元数据列表
	 */
	private static List<FieldMetadata> buildFieldMetadata(Class<?> clazz) {
		List<FieldMetadata> metadataList = new ArrayList<>(32);
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			Set<QueryField> queryFields = AnnotatedElementUtils.getAllMergedAnnotations(field, QueryField.class);

			if (CollUtil.isEmpty(queryFields)) {
				continue;
			}

			QueryConnectorEnum connectorEnum = !AnnotatedElementUtils.hasAnnotation(field, OrField.class)
					? QueryConnectorEnum.AND
					: QueryConnectorEnum.OR;

			metadataList.add(new FieldMetadata(field, queryFields, connectorEnum));
		}
		return metadataList;
	}

	public static SqlWhereCompose buildConditions(IQueryCriteria obj) {
		Class<?> clazz = obj.getClass();
		List<FieldMetadata> fieldMetadataList = getFieldMetadata(clazz);

		SqlWhereCompose compose = new SqlWhereCompose();

		for (FieldMetadata metadata : fieldMetadataList) {
			Field field = metadata.getField();
			try {
				boolean accessible = field.isAccessible();
				field.setAccessible(true);
				Object value = field.get(obj);

				// 使用缓存中的元数据构建查询条件
				QueryConnectorEnum connectorEnum = metadata.getConnectorEnum();

				SqlWhereCompose swc = new SqlWhereCompose();
				for (QueryField queryField : metadata.getQueryFields()) {
					// 忽略空值
					if (queryField.ignoreNull() && (value == null || value.toString().trim().isEmpty())) {
						continue;
					}
					if (queryField.ignoreCase() && value instanceof String) {
						value = ((String) value).toLowerCase();
					}

					SqlWhereInfo info = new SqlWhereInfo();
					info.setFieldName(SqlUtils.getFiledNameWithMapping(field));
					info.setFieldValue(value);
					info.setContrast(queryField.contrast());
					info.setValueType(value.getClass());
					info.setTableName(null);

					if (queryField.customWhereProvider() != ICustomWhereProvider.class) {
						info.setRawSql(SpringUtil.getBean(queryField.customWhereProvider())
								.getConditionSql(info.getFieldValue()));
					}
					SqlWhereNode<SqlWhereInfo> whereNode = new SqlWhereNode<>(
							queryField.connector() == null ? QueryConnectorEnum.AND : queryField.connector(), info);
					swc.getChildren().add(whereNode);
				}
				// 只有当swc有子节点时才添加到compose中
				if (!swc.getChildren().isEmpty()) {
					compose.getChildren().add(new SqlWhereNode<>(connectorEnum, swc));
				}
				field.setAccessible(accessible);
			} catch (IllegalAccessException e) {
				AssertUtil.throwError(
						() -> new FormattedRuntimeException(e, "Error accessing field: " + field.getName()));
			}
		}

		return compose;
	}

	public static <QO extends IQueryCriteria> ImportedSqlContext buildSqlContext(QO queryObject) {
		if (queryObject == null) {
			throw new FormattedRuntimeException("QO 不能为空");
		}
		var context = new ImportedSqlContext(() -> queryObject);
		context.rebuild();
		return context;
	}

}
