package com.tbs.sql.mapper.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.sql.annotation.condition.OrField;
import com.tbs.sql.annotation.condition.QueryField;
import com.tbs.sql.builder.ImportedSqlBuildContext;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.metadata.FieldMetadataCache;
import com.tbs.sql.model.where.WhereCompose;
import com.tbs.sql.model.where.WhereInfo;
import com.tbs.sql.model.where.WhereNode;
import com.tbs.sql.criteria.IQueryCriteria;
import com.tbs.sql.provider.ICustomWhereProvider;
import com.tbs.sql.util.SqlUtils;

import java.lang.reflect.Field;

/**
 * 查询条件构建器类，负责将查询条件对象转换为WHERE子句。
 *
 * <p>该类提供了将 {@link IQueryCriteria} 查询条件对象转换为 {@link WhereCompose} WHERE子句
 * 的功能。通过反射机制读取查询条件对象的字段值，根据注解配置构建相应的查询条件。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>将查询条件对象转换为WHERE子句 {@link #buildConditions}</li>
 *   <li>构建SQL构建上下文 {@link #buildSqlContext}</li>
 *   <li>支持多种查询条件类型</li>
 *   <li>支持自定义查询条件提供器</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在Mapper中构建查询条件</li>
 *   <li>将查询条件对象转换为SQL WHERE子句</li>
 *   <li>构建SQL执行上下文</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 构建查询条件
 * UserQuery query = new UserQuery();
 * query.setStatus(1);
 * WhereCompose whereCompose = ConditionBuilder.buildConditions(query);
 *
 * // 构建SQL上下文
 * ImportedSqlBuildContext context = ConditionBuilder.buildSqlContext(query);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>查询条件对象需要正确配置注解</li>
 *   <li>字段访问会暂时设置为可访问</li>
 *   <li>支持忽略空值和忽略大小写</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IQueryCriteria
 * @see WhereCompose
 * @see ImportedSqlBuildContext
 */
public class ConditionBuilder<T> {

    public static WhereCompose buildConditions(IQueryCriteria obj) {
        Class<?> clazz = obj.getClass();
        java.util.List<FieldMetadataCache.FieldMetadataEntry> fieldMetadataList = FieldMetadataCache.getFieldMetadata(clazz);
        WhereCompose compose = new WhereCompose();

        for (FieldMetadataCache.FieldMetadataEntry metadata : fieldMetadataList) {
            Field field = metadata.getField();
            try {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                Object value = field.get(obj);
                ConnectorEnum connectorEnum = metadata.getConnectorEnum();

                WhereCompose swc = new WhereCompose();
                for (QueryField queryField : metadata.getQueryFields()) {
                    if (queryField.ignoreNull() && (value == null || value.toString().trim().isEmpty())) continue;
                    if (queryField.ignoreCase() && value instanceof String) value = ((String) value).toLowerCase();

                    WhereInfo info = new WhereInfo();
                    info.setFieldName(SqlUtils.getFieldNameWithMapping(field));
                    info.setFieldValue(value);
                    info.setContrast(queryField.contrast());
                    info.setValueType(value.getClass());
                    info.setTableName(null);

                    if (queryField.customWhereProvider() != ICustomWhereProvider.class) {
                        info.setRawSql(SpringUtil.getBean(queryField.customWhereProvider()).getConditionSql(info.getFieldValue()));
                    }
                    swc.getChildren().add(new WhereNode<>(
                        queryField.connector() == null ? ConnectorEnum.AND : queryField.connector(), info));
                }
                if (!swc.getChildren().isEmpty()) {
                    compose.getChildren().add(new WhereNode<>(connectorEnum, swc));
                }
                field.setAccessible(accessible);
            } catch (IllegalAccessException e) {
                AssertUtil.throwError(() -> new FormattedRuntimeException(e, "Error accessing field: " + field.getName()));
            }
        }
        return compose;
    }

    public static <QO extends IQueryCriteria> ImportedSqlBuildContext buildSqlContext(QO queryObject) {
        AssertUtil.isTrue(queryObject == null, "queryObject cannot be null");
        var context = new ImportedSqlBuildContext(() -> queryObject);
        context.rebuild();
        return context;
    }
}
