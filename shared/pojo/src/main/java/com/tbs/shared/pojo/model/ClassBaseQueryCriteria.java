package com.tbs.shared.pojo.model;

import com.tbs.sql.model.SqlSelectInfoModel;
import com.tbs.sql.query.impl.AbstractResourceBaseQueryCriteria;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 类型查询对象
 *
 * @author abstergo
 */
public class ClassBaseQueryCriteria<T> extends AbstractResourceBaseQueryCriteria {

    @Getter
    @Setter
    private String sqlPrefix;

    @Getter
    @Setter
    private String sqlSuffix;

    private Class<T> type;

    public ClassBaseQueryCriteria(String sqlPrefix, String sqlSuffix) {
        this.sqlPrefix = sqlPrefix;
        this.sqlSuffix = sqlSuffix;
        String file = initString(getType(this));
        putResourcePath(file);
    }

    public ClassBaseQueryCriteria() {
        this.sqlPrefix = "sql/";
        this.sqlSuffix = ".sql";
        String file = initString(getType(this));
        putResourcePath(file);
    }

    String initString(Class<?> type) {
        return String.format("%s%s%s", getSqlPrefix(), type.getSimpleName(), getSqlSuffix());
    }

    @Override
    public SqlSelectInfoModel getSelectSQLInfo() {
        SqlSelectInfoModel selectInfoModel = super.getSelectSQLInfo();
        selectInfoModel.setEntityClass(type);
        return selectInfoModel;
    }

    public final Class<T> getType(ClassBaseQueryCriteria<T> target) {
        Type superClass = target.getClass().getGenericSuperclass();
        Class<T> type = null;
        if (superClass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)superClass;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                type = (Class<T>)actualTypeArguments[0];
            } else {
                throw new IllegalArgumentException("未找到泛型参数");
            }
        } else {
            throw new IllegalArgumentException("未指定泛型参数");
        }
        this.type = type;
        return type;
    }
}
