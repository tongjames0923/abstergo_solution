package com.tbs.sql.interceptor;

import cn.hutool.core.collection.CollUtil;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.IAutoValueProvider;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL自动填充MyBatis插件，用于在SQL执行前自动填充字段值。
 *
 * <p>填充发生在 {@link Executor#update} 拦截层、{@code invocation.proceed()} 之前。
 * 原因：MyBatis 在 {@code newStatementHandler} 内调用 {@link MappedStatement#getBoundSql(Object)}
 * 拼装最终SQL（含动态列清单），随后才执行 {@code ParameterHandler.setParameters}。若在后者填充，
 * 则选择性插入在生成SQL时就按非空字段判定列清单，主键因当时为null而被排除，导致雪花主键无法写入。
 * 只有在SQL生成前填充，字段才会进入列清单。</p>
 *
 * <p>填充失败直接中断当前写入：主键填充异常被吞掉时会插入一条主键为null的记录，
 * 事后无法定位也无法补录，因此这里不降级为告警日志。</p>
 *
 * @author abstergo
 * @see SqlAutoValue
 * @see IAutoValueProvider
 * @since 1.0.0
 */
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class SqlAutoFillInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(SqlAutoFillInterceptor.class);

    /** Provider实例缓存 */
    private static final Map<Class<? extends IAutoValueProvider>, IAutoValueProvider> providerCache =
        new ConcurrentHashMap<>(16);
    /** Class-带注解字段缓存，减少反射遍历开销 */
    private static final Map<Class<?>, List<Field>> classFieldCache = new ConcurrentHashMap<>(64);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement)args[0];
        SqlCommandType commandType = ms.getSqlCommandType();
        // 仅增删改需要填充，查询直接跳过
        if (SqlCommandType.INSERT == commandType || SqlCommandType.UPDATE == commandType) {
            // 必须在proceed()之前填充：此时SQL列清单尚未拼装
            doFill(args[1], commandType);
        }
        return invocation.proceed();
    }

    /** 处理入参：普通实体直接填充，Map入参递归提取其中的实体与集合 */
    private void doFill(Object paramObj, SqlCommandType commandType) {
        if (paramObj == null) {
            return;
        }
        if (paramObj instanceof Map) {
            // 用identity去重，避免同一集合/实体经多个别名key被重复遍历
            Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Object value : ((Map<?, ?>)paramObj).values()) {
                collectAndFill(value, commandType, visited);
            }
        } else {
            applyFill(paramObj, commandType);
        }
    }

    /** 递归收集实体：集合/数组逐元素下钻，其余对象直接填充 */
    private void collectAndFill(Object value, SqlCommandType commandType, Set<Object> visited) {
        if (value == null || !visited.add(value)) {
            return;
        }
        if (value instanceof Collection) {
            for (Object element : (Collection<?>)value) {
                collectAndFill(element, commandType, visited);
            }
        } else if (value instanceof Object[]) {
            for (Object element : (Object[])value) {
                collectAndFill(element, commandType, visited);
            }
        } else {
            applyFill(value, commandType);
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
                logger.error("自动填充字段[{}.{}]失败", clazz.getSimpleName(), field.getName(), e);
                AssertUtil.isTrue(true).i18n("sql.autofill.field.failed", clazz.getSimpleName(), field.getName(),
                    e.getMessage()).throwIt();
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
