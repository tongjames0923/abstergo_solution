package com.tbs.app.pojo.model;

import com.tbs.app.config.SystemSqlProperty;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.sql.query.impl.AbstractResourceBaseQueryCriteria;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 类型查询对象
 *
 * @author abstergo
 */
public class ClassBaseQueryCriteria<T> extends AbstractResourceBaseQueryCriteria {
    private static SystemSqlProperty sqlProperty;

    static String initString(Class<?> type) {
        AssertUtil.notNull(type, "type can not be null");
        if (sqlProperty == null) {
            sqlProperty = SpringUtil.getBean(SystemSqlProperty.class);
        }
        return String.format("%s%s%s", sqlProperty.getSqlPrefix(), type.getSimpleName(), sqlProperty.getSqlSuffix());
    }

    public final <T> Class<T> getType(ClassBaseQueryCriteria<T> target) {
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
        return type;
    }

    public ClassBaseQueryCriteria() {
        String file = initString(getType(this));
        setResourcePath(file);
    }
}
