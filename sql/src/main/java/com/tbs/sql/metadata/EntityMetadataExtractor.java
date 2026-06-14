package com.tbs.sql.metadata;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.annotation.core.SelectSQL;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.select.SelectItemModel;
import com.tbs.sql.util.SqlUtils;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体元数据提取器，从实体类提取SQL查询元数据
 *
 * @author abstergo
 * @since 1.0.0
 */
public class EntityMetadataExtractor {
    private static Map<Class<?>, SelectInfoModel> cache = new ConcurrentHashMap<>(32);

    public static void clearCache(Class<?> entityClass) {
        if (entityClass != null) {
            cache.remove(entityClass);
        }
    }

    public static void clearCache() {
        cache.clear();
    }

    public static SelectInfoModel pullFromClass(Class<?> c) {
        AssertUtil.notNull(c, "根据实体类型提取查询语句，实体类型不能为空");
        return cache.computeIfAbsent(c, entityClass -> {
            SelectInfoModel selectInfoModel = new SelectInfoModel();
            SelectSQL selectSQL = entityClass.getDeclaredAnnotation(SelectSQL.class);
            if (selectSQL != null && StrUtil.isNotEmpty(selectSQL.value())) {
                selectInfoModel.setRawSql(selectSQL.value());
                return selectInfoModel;
            }

            Table table = entityClass.getDeclaredAnnotation(Table.class);
            AssertUtil.notNull(table, "实体类" + entityClass.getName() + "没有添加@Table注解");
            selectInfoModel.setTableName(table.name());
            List<SelectItemModel> items = new LinkedList<>();
            Field[] fields = entityClass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (SqlUtils.isSkipField(field)) {
                    continue;
                }
                SelectItemModel item =
                    new SelectItemModel(SqlUtils.getFieldNameWithMapping(field), field.getName());
                items.add(item);
            }
            selectInfoModel.setEntityClass(entityClass);
            selectInfoModel.setFieldList(items);
            return selectInfoModel;
        });
    }
}
