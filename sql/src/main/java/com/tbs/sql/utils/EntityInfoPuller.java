package com.tbs.sql.utils;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.annotation.SelectSQL;
import com.tbs.sql.framework.model.SqlSelectInfoModel;
import com.tbs.sql.framework.model.SqlSelectItemModel;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体SQL数据提取器
 *
 * @author abstergo
 */
public class EntityInfoPuller {
    private static Map<Class<?>, SqlSelectInfoModel> cache = new ConcurrentHashMap<>(32);

    public static SqlSelectInfoModel pullFromClass(Class<?> c) {
        AssertUtil.notNull(c, "根据实体类型提取查询语句，实体类型不能为空");
        return cache.computeIfAbsent(c, entityClass -> {

            SqlSelectInfoModel sqlSelectInfoModel = new SqlSelectInfoModel();
            SelectSQL selectSQL = entityClass.getDeclaredAnnotation(SelectSQL.class);
            if (selectSQL != null && StrUtil.isNotEmpty(selectSQL.value())) {
                sqlSelectInfoModel.setRawSql(selectSQL.value());
                return sqlSelectInfoModel;
            }

            // 添加表名
            Table table = entityClass.getDeclaredAnnotation(Table.class);
            AssertUtil.notNull(table, "实体类" + entityClass.getName() + "没有添加@Table注解");
            sqlSelectInfoModel.setTableName(table.name());
            List<SqlSelectItemModel> items = new LinkedList<>();
            Field[] fields = entityClass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (SqlUtils.isSkipField(field)) {
                    continue;
                }
                SqlSelectItemModel item =
                    new SqlSelectItemModel(SqlUtils.getFiledNameWithMapping(field), field.getName());
                items.add(item);
            }
            sqlSelectInfoModel.setEntityClass(entityClass);
            sqlSelectInfoModel.setFieldList(items);
            return sqlSelectInfoModel;
        });

    }
}
