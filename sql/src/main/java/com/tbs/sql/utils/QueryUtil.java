// package tbs.framework.sql.utils;
package com.tbs.sql.utils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.core.annotation.AnnotatedElementUtils;

import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.BeanUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.sql.annotation.QueryField;
import com.tbs.sql.annotation.QueryFields;
import com.tbs.sql.annotation.QueryIndex;
import com.tbs.sql.annotation.QueryOrderField;
import com.tbs.sql.ef.IEntityQueryWrapper;
import com.tbs.sql.framework.ISqlAssembler;
import com.tbs.sql.framework.driver.IQueryDriver;
import com.tbs.sql.framework.driver.ISqlContext;
import com.tbs.sql.framework.extractor.ISelectSqlDialect;
import com.tbs.sql.framework.intercepter.ISqlInfoComposeIntercepter;
import com.tbs.sql.framework.model.SqlOrderInfo;
import com.tbs.sql.framework.model.SqlWhereCompose;
import com.tbs.sql.order.IQueryOrderBy;
import com.tbs.sql.properties.SqlProperty;
import com.tbs.sql.query.IQueryCriteria;

import cn.hutool.extra.spring.SpringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 查询工具类
 *
 * @author tongj
 */
@Slf4j
public class QueryUtil implements IStartup {

	// ==================== 单例相关 ====================

	@Getter
	private static QueryUtil instance;

	// 指定初始容量以减少扩容
	private static final Map<Class<?>, IEntityQueryWrapper> entityWrapperCache = new ConcurrentHashMap<>(64);

	// ==================== 核心组件 ====================

	/**
	 * SQL 查询方言
	 */
	private final ISelectSqlDialect selectSqlDialect;
	/**
	 * SQL 属性
	 */
	private final SqlProperty sqlProperty;
	private ISqlAssembler sqlAssembler;
	// 指定初始容量以减少扩容
	private final Map<Class<?>, List<Field>> fieldCache = new ConcurrentHashMap<>(128);

	private List<ISqlInfoComposeIntercepter> interceptors;

	public QueryUtil(ISelectSqlDialect dialect, SqlProperty sqlProperty, ISqlAssembler sqlAssembler) {
		this.selectSqlDialect = dialect;
		this.sqlProperty = sqlProperty;
		this.sqlAssembler = sqlAssembler;
		instance = this;
	}

	// ==================== 生命周期方法 ====================

	/**
	 * 获取实体包装器
	 *
	 * @param clazz  实体类
	 * @param entity 实体供应器
	 * @param <T>    实体类型
	 * @return 实体查询包装器
	 */
	public static final <T> IEntityQueryWrapper<T> getEntityQueryWrapper(Class<T> clazz, Supplier<T> entity) {
		return entityWrapperCache.computeIfAbsent(clazz, clazz1 -> {
			IEntityQueryWrapper<T> entityWrapper = SpringUtil.getBean(IEntityQueryWrapper.class);
			return entityWrapper.makeQueryable(entity.get());
		});
	}

	// ==================== 公共API ====================

	/**
	 * 获取实体包装器
	 *
	 * @param clazz 实体类
	 * @param <T>   实体类型
	 * @return 实体查询包装器
	 */
	public static final <T> IEntityQueryWrapper<T> getEntityQueryWrapper(Class<T> clazz) {
		try {
			return getEntityQueryWrapper(clazz, () -> {
				try {
					return BeanUtil.buildByEmptyArgs(clazz);
				} catch (Exception e) {
					return null;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取实体包装器
	 *
	 * @param entity 实体对象
	 * @param <T>    实体类型
	 * @return 实体查询包装器
	 */
	public static final <T> IEntityQueryWrapper<T> getEntityQueryWrapper(T entity) {
		return getEntityQueryWrapper((Class<T>) entity.getClass(), () -> entity);
	}

	@Override
	public void startUp() throws RuntimeException {
		try {
			// 获取所有拦截器并按顺序排序
			Collection<ISqlInfoComposeIntercepter> interceptorBeans = com.tbs.common.util.SpringUtil
					.getBeansOfType(ISqlInfoComposeIntercepter.class).values();
			this.interceptors = new ArrayList<>(interceptorBeans);
			// 预先排序以避免每次拦截时重复排序
			this.interceptors.sort(Comparator.comparingInt(ISqlInfoComposeIntercepter::getOrder));
		} catch (Exception e) {
			log.error("获取SQL拦截器失败", e);
			this.interceptors = new ArrayList<>(1);
		}
	}

	public static String driveSql(ISqlContext context, IQueryDriver driver) {
		ISelectSqlDialect dialect = com.tbs.common.util.SpringUtil.getBean(ISelectSqlDialect.class);
		ISqlAssembler assembler = com.tbs.common.util.SpringUtil.getBean(ISqlAssembler.class);
		return driveSql(dialect, assembler, context, driver);
	}

	public static String driveSql(ISelectSqlDialect dialect, ISqlAssembler assembler, ISqlContext context,
			IQueryDriver queryDriver) {

		AssertUtil.notNull(queryDriver, "查询驱动器不可为空");
		IQueryDriver.SqlQueryData data = queryDriver.getSqlQueryData(context);
		String selectSql = dialect.getSelectSqlExtractor().extractSelectSql(data.getSelectInfo());
		String whereSql = dialect.getWhereSqlExtractor().extraWhereSql(
				instance.intercept(data.getSqlWhereInfo(), context.getSqlEntityClass()), context.getSqlParams());

		String orderSql = dialect.getOrderSqlExtractor().extraOrderSql(data.getSqlOrders());
		String groupSql = dialect.getGroupSqlExtractor().extraGroupBySql(data.getSqlGroups());
		String limitSql = dialect.getLimitSqlExtractor().extraLimitSql(data.getSqlLimit());
		return assembler.assembleSelectSql(selectSql, whereSql, orderSql, groupSql, limitSql);
	}

	// ==================== 内部工具方法 ====================

	private SqlWhereCompose intercept(SqlWhereCompose compose, Class<?> entityClass) {
		// 使用增强for循环替代forEach以提高性能
		for (ISqlInfoComposeIntercepter interceptor : interceptors) {
			interceptor.intercept(compose, entityClass);
		}
		return compose;
	}

	private List<Field> collectFields(IQueryCriteria queryObject) {
		// 使用显式缓存检查替代computeIfAbsent以提高性能
		Class<?> clazz = queryObject.getClass();
		List<Field> cachedFields = fieldCache.get(clazz);
		if (cachedFields != null) {
			return cachedFields;
		}

		List<Field> fields = Arrays.stream(clazz.getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(QueryIndex.class)
						|| field.isAnnotationPresent(QueryField.class)
						|| field.isAnnotationPresent(QueryOrderField.class)
						|| AnnotatedElementUtils.hasAnnotation(field, QueryFields.class))
				.sorted(Comparator.comparingInt(this::getFieldIndex)).collect(Collectors.toList());

		fieldCache.put(clazz, fields);
		return fields;
	}

	private int getFieldIndex(Field field) {
		final QueryIndex annotation = field.getDeclaredAnnotation(QueryIndex.class);
		return (annotation != null) ? annotation.index() : 1000;
	}

	public static List<SqlOrderInfo> collectOrderInfos(IQueryCriteria queryObject) {
		List<Field> fields = instance.collectFields(queryObject);
		// 预先分配容量
		List<SqlOrderInfo> orderInfos = new ArrayList<>(fields.size() / 2);
		for (Field field : fields) {

			if (AnnotatedElementUtils.hasAnnotation(field, QueryOrderField.class)) {

				field.setAccessible(true);
				Object val = null;
				try {
					val = field.get(queryObject);
				} catch (IllegalAccessException e) {
					val = null;
					log.warn("Error accessing field: {}", field.getName(), e);
				}
				field.setAccessible(false);
				if (val == null) {
					log.trace("Field: {} is null skip order field", field.getName());
					continue;
				}

				QueryOrderField queryOrderField = field.getDeclaredAnnotation(QueryOrderField.class);
				SqlOrderInfo info = new SqlOrderInfo();
				var order = SpringUtil.getBean(queryOrderField.order(), IQueryOrderBy.class).orderBy(queryObject, field,
						queryOrderField.mappedName());
				if (order == null) {
					log.trace("order value: {} is null skip order field ", field.getName());
					continue;
				}
				info.setFiledName(
						StrUtil.isEmpty(queryOrderField.mappedName()) ? field.getName() : queryOrderField.mappedName());
				info.setOrder(order);
				orderInfos.add(info);
			}
		}
		return orderInfos;
	}

}
