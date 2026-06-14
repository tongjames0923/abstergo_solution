package com.tbs.sql.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.IAutoValueProvider;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.plugin.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL自动填充MyBatis插件
 *
 * @author abstergo
 * @since 1.0.0
 */
@Intercepts(@Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class}))
public class SqlAutoFillInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(SqlAutoFillInterceptor.class);
    private static final Map<Class<? extends IAutoValueProvider>, IAutoValueProvider> providerCache = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        ParameterHandler parameterHandler = (ParameterHandler) invocation.getTarget();
        Object parameters = parameterHandler.getParameterObject();
        if (parameters != null) {
            if (parameters instanceof Map) {
                ((Map<?, ?>) parameters).values().stream().distinct().filter(o -> o != null).forEach(this::applyValue);
            } else {
                applyValue(parameters);
            }
        }
        return invocation.proceed();
    }

    @NotNull
    private void applyValue(Object o) {
        if (o == null) return;
        Class<?> cas = o.getClass();
        for (Field field : cas.getDeclaredFields()) {
            Set<SqlAutoValue> f = AnnotatedElementUtils.getAllMergedAnnotations(field, SqlAutoValue.class);
            if (CollUtil.isEmpty(f)) continue;
            field.setAccessible(true);
            for (SqlAutoValue sqlAutoValue : f) {
                try {
                    IAutoValueProvider valueProvider = providerCache.computeIfAbsent(
                        sqlAutoValue.value(), SpringUtil::getBean);
                    if (valueProvider.support(field.getType(), field.get(o))) {
                        field.set(o, valueProvider.getValue(o, field));
                        logger.debug("自动填充字段{}，值为{}", field.getName(), field.get(o));
                    }
                } catch (Exception e) {
                    logger.debug("自动填充失败，请检查配置.{}", e);
                }
            }
        }
    }

    @Override
    public Object plugin(Object o) { return Plugin.wrap(o, this); }

    @Override
    public void setProperties(Properties properties) {}
}
