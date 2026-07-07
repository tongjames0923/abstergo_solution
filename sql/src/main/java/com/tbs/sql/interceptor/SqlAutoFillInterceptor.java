package com.tbs.sql.interceptor;

import cn.hutool.core.collection.CollUtil;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.IAutoValueProvider;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SQL自动填充MyBatis插件，用于在SQL执行前自动填充字段值。
 *
 * <p>该类同时拦截两个层级：</p>
 * <ul>
 *   <li>{@link Executor#update} 层：捕获当前SQL的 {@link SqlCommandType}，存入上下文ThreadLocal</li>
 *   <li>{@link ParameterHandler#setParameters} 层：根据SQL类型和注解属性执行自动填充</li>
 * </ul>
 *
 * @author abstergo
 * @see SqlAutoValue
 * @see IAutoValueProvider
 * @since 1.0.0
 */
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class})})
public class SqlAutoFillInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(SqlAutoFillInterceptor.class);

    /** Provider实例缓存 */
    private static final Map<Class<? extends IAutoValueProvider>, IAutoValueProvider> providerCache =
        new ConcurrentHashMap<>(16);
    /** Class-带注解字段缓存，减少反射遍历开销 */
    private static final Map<Class<?>, List<Field>> classFieldCache = new ConcurrentHashMap<>(64);

    /** 单次SQL上下文，隔离ThreadLocal，避免多条SQL类型互相覆盖 */
    private static final ThreadLocal<SqlContext> SQL_CONTEXT = new ThreadLocal<>();

    /** SQL上下文载体 */
    private static class SqlContext {
        private final SqlCommandType commandType;

        public SqlContext(SqlCommandType commandType) {
            this.commandType = commandType;
        }

        public SqlCommandType getCommandType() {
            return commandType;
        }
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        if (target instanceof Executor) {
            return interceptExecutor(invocation);
        }
        if (target instanceof ParameterHandler) {
            return interceptParameterHandler(invocation);
        }
        return invocation.proceed();
    }

    /** 拦截Executor.update，绑定当前SQL类型上下文 */
    private Object interceptExecutor(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement)invocation.getArgs()[0];
        SqlCommandType commandType = ms.getSqlCommandType();
        // 仅增删改需要填充，查询直接跳过上下文绑定
        if (SqlCommandType.INSERT == commandType || SqlCommandType.UPDATE == commandType) {
            SQL_CONTEXT.set(new SqlContext(commandType));
        }
        try {
            return invocation.proceed();
        } finally {
            // 强制清除，杜绝内存泄漏
            SQL_CONTEXT.remove();
        }
    }

    /** 拦截参数赋值，执行字段自动填充 */
    private Object interceptParameterHandler(Invocation invocation) throws Throwable {
        SqlContext context = SQL_CONTEXT.get();
        // 无上下文说明是SELECT/DELETE，无需填充
        if (context == null) {
            return invocation.proceed();
        }
        SqlCommandType commandType = context.getCommandType();
        ParameterHandler parameterHandler = (ParameterHandler)invocation.getTarget();
        Object paramObj = parameterHandler.getParameterObject();
        if (paramObj == null) {
            return invocation.proceed();
        }
        // 处理Map参数
        if (paramObj instanceof Map) {
            handleMapParam((Map<?, ?>)paramObj, commandType);
        } else {
            // 普通实体对象
            applyFill(paramObj, commandType);
        }
        return invocation.proceed();
    }

    /** 处理MyBatis Map入参，过滤内置参数，只处理业务实体 */
    private void handleMapParam(Map<?, ?> paramMap, SqlCommandType commandType) {
        // MyBatis内置参数key过滤，不处理
        Set<String> excludeKeys = new HashSet<>(Arrays.asList("param1", "param2", "collection", "list", "array"));
        List<Object> entityList =
            paramMap.entrySet().stream().filter(entry -> !excludeKeys.contains(String.valueOf(entry.getKey())))
                .map(Map.Entry::getValue).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        for (Object entity : entityList) {
            applyFill(entity, commandType);
        }
    }

    /** 核心填充逻辑：递归扫描当前类+父类带@SqlAutoValue字段并赋值 */
    private void applyFill(Object entity, SqlCommandType commandType) {
        Class<?> clazz = entity.getClass();
        // 从缓存获取带注解字段，无则扫描并缓存
        List<Field> autoFillFields = classFieldCache.computeIfAbsent(clazz, this::scanAllAutoFillField);
        if (CollUtil.isEmpty(autoFillFields)) {
            return;
        }
        for (Field field : autoFillFields) {
            Set<SqlAutoValue> annoSet = AnnotatedElementUtils.getAllMergedAnnotations(field, SqlAutoValue.class);
            if (CollUtil.isEmpty(annoSet)) {
                continue;
            }
            // 去重，同一字段只取一个注解
            SqlAutoValue sqlAutoValue = annoSet.iterator().next();
            // 仅新增填充 && 当前不是INSERT，跳过
            if (sqlAutoValue.insertOnly() && SqlCommandType.INSERT != commandType) {
                continue;
            }
            try {
                Object originalVal = field.get(entity);
                // 配置仅为空时填充，且已有值则跳过
                if (sqlAutoValue.fillWhenNull() && originalVal != null) {
                    continue;
                }
                // 获取Provider（双重检查锁防并发重复初始化）
                IAutoValueProvider provider = getAutoValueProvider(sqlAutoValue.value());
                if (provider.support(field.getType(), originalVal)) {
                    Object newVal = provider.getValue(entity, field);
                    field.set(entity, newVal);
                    logger.debug("自动填充字段[{}.{}], 原值={}, 填充值={}", clazz.getSimpleName(), field.getName(),
                        originalVal, newVal);
                }
            } catch (Exception e) {
                logger.warn("自动填充字段{}失败，异常信息：{}#{}", field.getName(), e.getClass().getSimpleName(),
                    e.getMessage());
            }
        }
    }

    /** 递归扫描当前类+所有父类，收集所有带@SqlAutoValue的字段 */
    private List<Field> scanAllAutoFillField(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        Class<?> tempCls = clazz;
        while (tempCls != Object.class) {
            Field[] declaredFields = tempCls.getDeclaredFields();
            for (Field field : declaredFields) {
                Set<SqlAutoValue> anno = AnnotatedElementUtils.getAllMergedAnnotations(field, SqlAutoValue.class);
                if (CollUtil.isNotEmpty(anno)) {
                    field.setAccessible(true);
                    fieldList.add(field);
                }
            }
            tempCls = tempCls.getSuperclass();
        }
        return fieldList;
    }

    /** 双重检查锁获取Provider，缓存复用 */
    private IAutoValueProvider getAutoValueProvider(Class<? extends IAutoValueProvider> providerCls) {
        IAutoValueProvider provider = providerCache.get(providerCls);
        if (provider == null) {
            synchronized (providerCache) {
                provider = providerCache.get(providerCls);
                if (provider == null) {
                    provider = BeanUtil.buildBeanFromProperties(null, providerCls, BeanUtil::buildByEmptyArgs);
                    providerCache.put(providerCls, provider);
                }
            }
        }
        return provider;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 无需配置参数
    }
}
