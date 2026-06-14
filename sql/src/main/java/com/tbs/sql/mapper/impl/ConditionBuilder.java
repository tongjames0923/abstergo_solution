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
 * 查询条件构建器（原 FrameworkSelectProvider）
 *
 * @author abstergo
 * @since 2.0.0
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
        if (queryObject == null) throw new FormattedRuntimeException("QO 不能为空");
        var context = new ImportedSqlBuildContext(() -> queryObject);
        context.rebuild();
        return context;
    }
}
