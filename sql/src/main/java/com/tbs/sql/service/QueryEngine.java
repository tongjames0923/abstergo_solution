package com.tbs.sql.service;

import cn.hutool.extra.spring.SpringUtil;
import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.sql.assembler.ISqlAssembler;
import com.tbs.sql.builder.SqlBuildContext;
import com.tbs.sql.builder.SqlDriver;
import com.tbs.sql.criteria.IQueryCriteria;
import com.tbs.sql.dialect.SqlDialect;
import com.tbs.sql.dsl.IEntityQueryWrapper;
import com.tbs.sql.interceptor.SqlComposeInterceptor;
import com.tbs.sql.model.order.OrderInfo;
import com.tbs.sql.model.where.WhereCompose;
import com.tbs.sql.properties.SqlProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL查询引擎（原 QueryUtil）
 *
 * @author abstergo
 * @since 2.0.0
 */
@Slf4j
public class QueryEngine implements IStartup {

    @Getter
    private static volatile QueryEngine instance;

    private final SqlDialect dialect;
    private final SqlProperty sqlProperty;
    private final ISqlAssembler sqlAssembler;
    private final Map<Class<?>, List<OrderFieldMetadata>> orderFieldCache = new ConcurrentHashMap<>(16);
    private static final Map<String, com.tbs.sql.order.IQueryOrderBy> ORDER_BEAN_CACHE = new ConcurrentHashMap<>(8);

    /**
     * 排序字段元数据，缓存注解信息避免重复反射。
     */
    private static class OrderFieldMetadata {
        final Field field;
        final String orderBeanName;
        final String mappedName;

        OrderFieldMetadata(Field field, String orderBeanName, String mappedName) {
            this.field = field;
            this.orderBeanName = orderBeanName;
            this.mappedName = mappedName;
        }
    }
    private List<SqlComposeInterceptor> interceptors;

    /**
     * 构造函数，初始化SQL查询引擎。
     *
     * @param dialect SQL方言，用于处理不同数据库的SQL语法差异
     * @param sqlProperty SQL配置属性
     * @param sqlAssembler SQL组装器，用于组装SQL语句
     */
    public QueryEngine(SqlDialect dialect, SqlProperty sqlProperty, ISqlAssembler sqlAssembler) {
        this.dialect = dialect;
        this.sqlProperty = sqlProperty;
        this.sqlAssembler = sqlAssembler;
    }

    // ==================== 实体包装器工厂方法 ====================

    /**
     * 创建实体查询包装器，使用自定义实体供应商。
     *
     * @param clazz 实体类类型
     * @param entity 实体供应商，用于创建实体对象
     * @param <T> 实体类型
     * @return 实体查询包装器实例
     */
    @SuppressWarnings("unchecked")
    public static final <T> IEntityQueryWrapper<T> getEntityQueryWrapper(Class<T> clazz, java.util.function.Supplier<T> entity) {
        try {
            IEntityQueryWrapper<T> entityWrapper = SpringUtil.getBean(IEntityQueryWrapper.class);
            return entityWrapper.makeQueryable(entity.get());
        } catch (Exception e) {
            AssertUtil.throwError(() -> new FormattedRuntimeException(e, "Failed to create entity wrapper for class: {}", clazz.getName()));
        }
        return null;
    }

    /**
     * 创建实体查询包装器，使用默认实体创建方式。
     *
     * @param clazz 实体类类型
     * @param <T> 实体类型
     * @return 实体查询包装器实例
     */
    public static final <T> IEntityQueryWrapper<T> getEntityQueryWrapper(Class<T> clazz) {
        try {
            return getEntityQueryWrapper(clazz, () -> BeanUtil.buildByEmptyArgs(clazz));
        } catch (Exception e) {
            AssertUtil.throwError(() -> new FormattedRuntimeException("Failed to create entity wrapper for class: {}", clazz.getName()));
        }
        return null;
    }

    /**
     * 创建实体查询包装器，使用现有实体对象。
     *
     * @param entity 实体对象
     * @param <T> 实体类型
     * @return 实体查询包装器实例
     */
    @SuppressWarnings("unchecked")
    public static final <T> IEntityQueryWrapper<T> getEntityQueryWrapper(T entity) {
        return getEntityQueryWrapper((Class<T>) entity.getClass(), () -> entity);
    }

    // ==================== 生命周期 ====================

    @Override
    public void startUp() throws RuntimeException {
        try {
            Collection<SqlComposeInterceptor> interceptorBeans =
                SpringUtil.getBeansOfType(SqlComposeInterceptor.class).values();
            this.interceptors = new ArrayList<>(interceptorBeans);
            this.interceptors.sort(Comparator.comparingInt(SqlComposeInterceptor::getOrder));
            instance = this;
        } catch (Exception e) {
            log.error("获取SQL拦截器失败", e);
            this.interceptors = new ArrayList<>(1);
        }
    }

    // ==================== 核心SQL构建 ====================

    /**
     * 使用默认方言和组装器驱动SQL生成。
     *
     * @param context SQL构建上下文
     * @param driver SQL驱动器
     * @return 生成的SQL语句
     */
    public String driveSql(SqlBuildContext context, SqlDriver driver) {
        AssertUtil.notNull(driver, "查询驱动器不可为空");
        SqlDialect d = this.dialect;
        ISqlAssembler a = this.sqlAssembler;
        return driveSql(d, a, context, driver);
    }

    /**
     * 使用指定方言和组装器驱动SQL生成。
     *
     * @param dialect SQL方言
     * @param assembler SQL组装器
     * @param context SQL构建上下文
     * @param queryDriver SQL驱动器
     * @return 生成的SQL语句
     */
    public String driveSql(SqlDialect dialect, ISqlAssembler assembler, SqlBuildContext context, SqlDriver queryDriver) {
        AssertUtil.notNull(queryDriver, "查询驱动器不可为空");
//        var uuid = UUID.randomUUID().toString();
//
//        Date date = new Date();
        SqlDriver.SqlQueryData data = queryDriver.getSqlQueryData(context);
        String selectSql = dialect.getSelectExtractor().extract(data.getSelectInfo());
        String whereSql = dialect.getWhereExtractor().extract(
            getInstance().intercept(data.getSqlWhereInfo(), context.getEntityClass()), context.getSqlParams());
        String orderSql = dialect.getOrderExtractor().extract(data.getSqlOrders());
        String groupSql = dialect.getGroupExtractor().extract(data.getSqlGroups());
        String limitSql = dialect.getLimitExtractor().extract(data.getSqlLimit());
        var sql = assembler.assembleSelectSql(selectSql, whereSql, orderSql, groupSql, limitSql);
//        Date date1 = new Date();
//        log.info("SQL组装完成 {} {} 总耗时：{}ms", uuid, sql, date1.getTime() - date.getTime());
        return sql;
    }

    // ==================== 内部方法 ====================

    /**
     * 执行拦截器链处理。
     *
     * @param compose WHERE条件组合
     * @param entityClass 实体类类型
     * @return 处理后的WHERE条件组合
     */
    private WhereCompose intercept(WhereCompose compose, Class<?> entityClass) {
        for (SqlComposeInterceptor interceptor : interceptors) {
            compose = interceptor.intercept(compose, entityClass);
        }
        return compose;
    }

    /**
     * 收集查询对象中带注解的字段（复用 FieldMetadataCache 缓存）。
     *
     * @param queryObject 查询对象
     * @return 带注解的字段列表
     */
    private List<Field> collectFields(IQueryCriteria queryObject) {
        return com.tbs.sql.metadata.FieldMetadataCache.collectAnnotatedFields(queryObject.getClass());
    }

    /**
     * 获取字段的索引值。
     *
     * @param field 字段
     * @return 字段索引值，默认为1000
     */
    private int getFieldIndex(Field field) {
        final com.tbs.sql.annotation.condition.QueryIndex annotation = field.getDeclaredAnnotation(com.tbs.sql.annotation.condition.QueryIndex.class);
        return (annotation != null) ? annotation.index() : 1000;
    }

    /**
     * 收集查询对象中带排序注解的字段元数据（带缓存）。
     *
     * @param queryObject 查询对象
     * @return 带排序注解的字段元数据列表
     */
    private List<OrderFieldMetadata> collectOrderFields(IQueryCriteria queryObject) {
        Class<?> clazz = queryObject.getClass();
        return orderFieldCache.computeIfAbsent(clazz, (cla) -> Arrays.stream(cla.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(com.tbs.sql.annotation.order.QueryOrderField.class))
            .sorted(Comparator.comparingInt(this::getFieldIndex))
            .map(field -> {
                com.tbs.sql.annotation.order.QueryOrderField annotation = field.getDeclaredAnnotation(com.tbs.sql.annotation.order.QueryOrderField.class);
                if (annotation == null || com.tbs.common.util.base.StrUtil.isEmpty(annotation.order()) || com.tbs.common.util.base.StrUtil.isEmpty(annotation.mappedName())) {
                    return null;
                }
                return new OrderFieldMetadata(field, annotation.order(), annotation.mappedName());
            })
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * 收集查询对象中的排序信息。
     *
     * @param queryObject 查询对象
     * @return 排序信息列表
     */
    public static List<OrderInfo> collectOrderInfos(IQueryCriteria queryObject) {
        if (getInstance() == null) {
            throw new FormattedRuntimeException("QueryEngine尚未初始化，请确保@EnableTbsSql已启用且应用已启动");
        }
        // 使用缓存的排序字段元数据，避免重复反射和注解检查
        List<OrderFieldMetadata> metadataList = getInstance().collectOrderFields(queryObject);
        List<OrderInfo> orderInfos = new ArrayList<>(metadataList.size());
        for (OrderFieldMetadata metadata : metadataList) {
            Field field = metadata.field;

            // 优化反射访问：使用缓存的可访问性
            boolean originalAccessible = field.isAccessible();
            field.setAccessible(true);
            Object val = null;
            try { val = field.get(queryObject); } catch (IllegalAccessException e) { val = null; log.warn("Error accessing field: {}", field.getName(), e); } finally { field.setAccessible(originalAccessible); }
            if (val == null) { log.debug("排序字段 {} 值为null，跳过", field.getName()); continue; }

            OrderInfo info = new OrderInfo();
            // 使用缓存的Bean查找
            com.tbs.sql.order.IQueryOrderBy order = getOrderByBean(metadata.orderBeanName);
            if (order == null) { log.debug("排序Bean为空，跳过 {}", field.getName()); continue; }

            com.tbs.sql.enums.OrderEnum orderEnum = null;
            try { orderEnum = order.orderBy(queryObject, field, metadata.mappedName); } catch (Exception e) { log.warn("Error getting order enum: {}", field.getName(), e); continue; }
            if (orderEnum == null) { log.debug("排序方向为null，跳过 {}", field.getName()); continue; }

            info.setFieldName(metadata.mappedName);
            info.setOrder(orderEnum);
            orderInfos.add(info);
        }
        return orderInfos;
    }

    /**
     * 获取排序策略Bean（带缓存）。
     *
     * @param beanName Bean名称
     * @return 排序策略Bean实例，如果获取失败则返回null
     */
    private static com.tbs.sql.order.IQueryOrderBy getOrderByBean(String beanName) {
        return ORDER_BEAN_CACHE.computeIfAbsent(beanName, name -> {
            try {
                return SpringUtil.getBean(name, com.tbs.sql.order.IQueryOrderBy.class);
            } catch (Exception e) {
                log.warn("Error getting order bean: {}", name, e);
                return null;
            }
        });
    }
}
