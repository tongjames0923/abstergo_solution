package com.tbs.shared.pojo.model;

import com.tbs.common.error.AssertUtil;
import com.tbs.sql.framework.model.SqlSelectInfoModel;
import com.tbs.sql.query.AbstractResourceBaseQueryCriteria;
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
        AssertUtil.toAssert(!(superClass instanceof ParameterizedType)).message("类型参数不存在").throwIt();
        ParameterizedType parameterizedType = (ParameterizedType)superClass;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        AssertUtil.toAssert(actualTypeArguments.length <= 0).message("未找到泛型参数").throwIt();
        type = (Class<T>)actualTypeArguments[0];
        this.type = type;
        return type;
    }
}
