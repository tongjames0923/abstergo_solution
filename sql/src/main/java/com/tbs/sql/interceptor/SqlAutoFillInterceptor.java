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
 * SQL自动填充MyBatis插件，用于在SQL执行前自动填充字段值。
 *
 * <p>该类实现了MyBatis的 {@link Interceptor} 接口，通过拦截 {@link ParameterHandler}
 * 的setParameters方法，在SQL执行前自动填充标注了 {@link SqlAutoValue} 注解的字段值。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>自动填充标注了 {@link SqlAutoValue} 注解的字段</li>
 *   <li>支持自定义值提供器 {@link IAutoValueProvider}</li>
 *   <li>支持Map和实体对象两种参数类型</li>
 *   <li>缓存值提供器实例，提高性能</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>自动填充创建时间、更新时间等时间字段</li>
 *   <li>自动生成UUID、序列号等唯一标识</li>
 *   <li>根据业务规则自动计算字段值</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在实体类中使用
 * public class User {
 *     @AutoSetDate
 *     private Date createTime;
 *     
 *     @SqlAutoValue(value = CustomProvider.class)
 *     private String customField;
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>需要在配置中启用 {@code tbs.sql.enable-auto-fill-value-interceptor: true}</li>
 *   <li>值提供器需要注册为Spring Bean</li>
 *   <li>填充失败会记录调试日志，不会中断SQL执行</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see SqlAutoValue
 * @see IAutoValueProvider
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
