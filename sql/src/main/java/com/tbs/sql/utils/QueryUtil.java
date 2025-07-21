// package tbs.framework.sql.utils;
package com.tbs.sql.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.tbs.common.util.BeanUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.sql.annotation.*;
import com.tbs.sql.ef.IEntityQueryWrapper;
import com.tbs.sql.enums.QueryConnectorEnum;
import com.tbs.sql.exception.SqlExtractionException;
import com.tbs.sql.extractor.ISelectSqlDialect;
import com.tbs.sql.model.*;
import com.tbs.sql.order.IQueryOrderBy;
import com.tbs.sql.properties.SqlProperty;
import com.tbs.sql.provider.ICustomWhereProvider;
import com.tbs.sql.query.IPage;
import com.tbs.sql.query.IQueryCriteria;
import com.tbs.sql.query.ISqlAssembler;
import lombok.Getter;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 查询工具类
 *
 * @author tongj
 */
public class QueryUtil {

    @Getter
    private static QueryUtil instance;

    /**
     * SQL 查询方言
     */
    private final ISelectSqlDialect selectSqlDialect;
    /**
     * SQL 属性
     */
    private final SqlProperty sqlProperty;

    private ISqlAssembler sqlAssembler;

    private Map<Class, List<Field>> fieldCache = new ConcurrentHashMap<>();

    public QueryUtil(ISelectSqlDialect dialect, SqlProperty sqlProperty, ISqlAssembler sqlAssembler) {
        this.selectSqlDialect = dialect;
        this.sqlProperty = sqlProperty;
        this.sqlAssembler = sqlAssembler;
        instance = this;
    }

    // 缓存Map，用于存储每个类的字段信息
    private final Map<Class<?>, List<FieldMetadata>> fieldMetadataCache = new ConcurrentHashMap<>();

    private List<Field> collectFields(Object queryObject) {
        return fieldCache.computeIfAbsent(queryObject.getClass(), clazz -> {
            return Arrays.stream(clazz.getDeclaredFields()).filter(
                    field -> field.isAnnotationPresent(QueryIndex.class) ||
                        field.isAnnotationPresent(QueryField.class) ||
                        field.isAnnotationPresent(QueryOrderField.class) ||
                        AnnotatedElementUtils.hasAnnotation(field, QueryFields.class))
                .sorted(Comparator.comparingInt(this::getFieldIndex)).collect(Collectors.toList());
        });
    }

    private int getFieldIndex(Field field) {
        final QueryIndex annotation = field.getDeclaredAnnotation(QueryIndex.class);
        return (annotation != null) ? annotation.index() : 1000;
    }

    private SqlWhereCompose buildConditions(Object obj) {
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
                        value = ((String)value).toLowerCase();
                    }

                    SqlWhereInfo info = new SqlWhereInfo();
                    info.setFieldName(SqlUtils.getFiledNameWithMapping(field));
                    info.setFieldValue(value);
                    info.setContrast(queryField.contrast());
                    info.setValueType(value.getClass());
                    info.setTableName(null);

                    if (queryField.customWhereProvider() != ICustomWhereProvider.class) {
                        info.setRawSql(
                            SpringUtil.getBean(queryField.customWhereProvider()).getConditionSql(info.getFieldValue()));
                    }
                    SqlWhereNode<SqlWhereInfo> whereNode = new SqlWhereNode<>(
                        queryField.connector() == null ? QueryConnectorEnum.AND : queryField.connector(), info);
                    swc.getChildren().add(whereNode);
                }
                compose.getChildren().add(new SqlWhereNode(connectorEnum, swc));
                field.setAccessible(accessible);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error accessing field: " + field.getName(), e);
            }
        }

        return compose;
    }

    /**
     * 获取字段元数据
     *
     * @param clazz 类对象
     * @return 字段元数据列表
     */
    private List<FieldMetadata> getFieldMetadata(Class<?> clazz) {
        return fieldMetadataCache.computeIfAbsent(clazz, this::buildFieldMetadata);
    }

    /**
     * 构建字段元数据
     *
     * @param clazz 类对象
     * @return 字段元数据列表
     */
    private List<FieldMetadata> buildFieldMetadata(Class<?> clazz) {
        List<FieldMetadata> metadataList = new ArrayList<>(32);
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            Set<QueryField> queryFields = AnnotatedElementUtils.getAllMergedAnnotations(field, QueryField.class);

            if (CollUtil.isEmpty(queryFields)) {
                continue;
            }

            QueryConnectorEnum connectorEnum =
                !AnnotatedElementUtils.hasAnnotation(field, OrField.class) ? QueryConnectorEnum.AND
                    : QueryConnectorEnum.OR;

            metadataList.add(new FieldMetadata(field, queryFields, connectorEnum));
        }
        return metadataList;
    }

    /**
     * 表字段元数据类，用于封装字段的元数据信息
     */
    private static class FieldMetadata {
        /**
         * -- GETTER -- 获取字段对象
         *
         * @return 字段对象
         */
        // 字段对象
        @Getter
        private final Field field;
        // 查询字段集合，用于表示该字段可以进行哪些查询操作
        private final Set<QueryField> queryFields;
        /**
         * -- GETTER -- 获取查询连接器枚举
         *
         * @return 查询连接器枚举
         */
        // 查询连接器枚举，定义了字段之间的查询关系
        @Getter
        private final QueryConnectorEnum connectorEnum;
        private List<QueryField> queryFieldList;

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
            queryFieldList =
                queryFields.stream().sorted(Comparator.comparingInt(QueryField::index)).collect(Collectors.toList());
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

    /**
     * 缓存Map，用于存储每个类的字段信息
     */
    private List<SqlOrderInfo> collectOrderInfos(Object queryObject) {
        List<Field> fields = collectFields(queryObject);
        List<SqlOrderInfo> orderInfos = new ArrayList<>(fields.size());
        for (Field field : fields) {
            if (AnnotatedElementUtils.hasAnnotation(field, QueryOrderField.class)) {
                QueryOrderField queryOrderField = field.getDeclaredAnnotation(QueryOrderField.class);
                SqlOrderInfo info = new SqlOrderInfo();
                info.setFiledName(
                    StrUtil.isEmpty(queryOrderField.mappedName()) ? field.getName() : queryOrderField.mappedName());
                info.setOrder(SpringUtil.getBean(queryOrderField.order(), IQueryOrderBy.class)
                    .orderBy(queryObject, field, queryOrderField.mappedName()));
                orderInfos.add(info);
            }
        }
        return orderInfos;
    }

    /**
     * 根据查询对象构建查询SQL
     *
     * @param queryObject 查询对象
     * @param page        分页信息
     * @param params      参数 用于填充参数
     * @return 查询SQL
     */
    public <T extends IQueryCriteria> String getQuery(T queryObject, IPage page, Map<String, Object> params) {
        if (queryObject == null) {
            throw new SqlExtractionException("Query object cannot be null", this);
        }
        String selectSql = selectSqlDialect.getSelectSqlExtractor().extractSelectSql(queryObject.getSelectSQLInfo());
        SqlWhereCompose compose = buildConditions(queryObject);
        String whereSql = selectSqlDialect.getWhereSqlExtractor().extraWhereSql(compose, params);
        String orderSql = selectSqlDialect.getOrderSqlExtractor().extraOrderSql(collectOrderInfos(queryObject));
        String pageSql = "";
        if (page != null) {
            pageSql =
                selectSqlDialect.getLimitSqlExtractor().extraLimitSql(new SqlLimitModel(page.from(), page.number()));
        }
        return sqlAssembler.assemble(selectSql, whereSql, orderSql, "", pageSql);
    }

    /**
     * 根据查询对象构建查询SQL
     *
     * @param fetchObject 查询对象
     * @param params      参数 用于填充参数
     * @return 查询SQL
     */
    public <T> String getQueryByInfos(FetchObject fetchObject, Map<String, Object> params) {
        if (fetchObject.getEntity() == null) {
            throw new SqlExtractionException("Entity cannot be null", this);
        }
        String baseSelect = "";
        if (BeanUtil.isBaseFrom(fetchObject.getEntity().getClass(), IQueryCriteria.class)) {
            baseSelect = selectSqlDialect.getSelectSqlExtractor()
                .extractSelectSql(((IQueryCriteria)fetchObject.getEntity()).getSelectSQLInfo());
        } else {
            baseSelect = selectSqlDialect.getSelectSqlExtractor()
                .extractSelectSql(EntityInfoPuller.pullFromClass(fetchObject.getEntity().getClass()));
        }

        String whereSql = selectSqlDialect.getWhereSqlExtractor().extraWhereSql(fetchObject.getWhereCompose(), params);
        String orderSql = selectSqlDialect.getOrderSqlExtractor().extraOrderSql(fetchObject.getOrderInfos());
        String pageSql = "";
        SqlLimitModel page = fetchObject.getLimitModel();
        if (page != null && (page.getCount() != null && page.getCount() > 0L)) {
            pageSql = selectSqlDialect.getLimitSqlExtractor()
                .extraLimitSql(new SqlLimitModel(page.getOffset() == null ? 0L : page.getOffset(), page.getCount()));
        }
        String groupBySql = "";
        if (CollUtil.isNotEmpty(fetchObject.getSqlGroupModels())) {
            groupBySql = selectSqlDialect.getGroupSqlExtractor().extraGroupBySql(fetchObject.getSqlGroupModels());
        }
        return sqlAssembler.assemble(baseSelect, whereSql, orderSql, groupBySql, pageSql);
    }

    private static Map<Class<?>, IEntityQueryWrapper> entityWrapperCache = new ConcurrentHashMap<>();

    /**
     * 获取实体包装器
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static final <T> IEntityQueryWrapper<T> getEntityQueryWrapper(Class<T> clazz, Supplier<T> entity) {
        return entityWrapperCache.computeIfAbsent(clazz, clazz1 -> {
            IEntityQueryWrapper<T> entityWrapper = SpringUtil.getBean(IEntityQueryWrapper.class);
            return entityWrapper.makeQueryable(entity.get());
        });
    }

    /**
     * 获取实体包装器
     *
     * @param clazz
     * @param <T>
     * @return
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
     * @param entity
     * @param <T>
     * @return
     */
    public static final <T> IEntityQueryWrapper<T> getEntityQueryWrapper(T entity) {
        return getEntityQueryWrapper((Class<T>)entity.getClass(), () -> {
            return entity;
        });
    }
}
