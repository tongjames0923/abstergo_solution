package com.tbs.sql.mapper;

import org.springframework.util.ClassUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapper 接口的实体泛型解析工具。
 *
 * <p>从 Mapper 接口沿 {@code getGenericInterfaces()} 递归向下查找，逐层绑定类型变量，
 * 解析 {@link IFrameworkMapper} / {@link GenericMapper} 声明的第一个泛型实参作为实体类型。
 * 因此 {@code XxxMapper extends BaseMapper<Xxx>} 这类中间基接口同样可以解析。</p>
 *
 * <p>MyBatis 的 Mapper 实例是 JDK 动态代理，default 方法里的 {@code this.getClass()} 是代理类，
 * 不能直接当 Mapper 接口使用；{@link #resolveMapperType(Object)} 通过
 * {@code ClassUtils.getAllInterfaces(mapperProxy)} 从代理实例反查最具体的 Mapper 接口，
 * 再交给 {@link #resolveEntityClass(Class)} 解析实体类型。</p>
 *
 * <p>本工具无状态，供 {@code GenericSqlProvider} 与启动期绑定注册表共用，保证 Provider 侧与
 * DSL 侧对同一 Mapper 的实体认定一致。</p>
 *
 * @author abstergo
 * @since 3.0.0
 */
public final class MapperEntityTypeResolver {

    private MapperEntityTypeResolver() {
    }

    /**
     * 解析 Mapper 接口声明的实体类型。
     *
     * @param mapperType Mapper 接口类型，可为 null
     * @return 实体类型；未按约定声明泛型时返回 null
     */
    public static Class<?> resolveEntityClass(Class<?> mapperType) {
        if (mapperType == null) {
            return null;
        }
        for (Type type : mapperType.getGenericInterfaces()) {
            Class<?> entityClass = resolveEntityClass(type, Collections.emptyMap());
            if (entityClass != null) {
                return entityClass;
            }
        }
        return null;
    }

    /**
     * 从 Mapper 实例（通常是 JDK 动态代理）反查最具体的 Mapper 接口。
     *
     * @param mapperProxy Mapper 实例，可为 null
     * @return 继承自 {@link IFrameworkMapper} 的最具体接口；找不到时返回 null
     */
    public static Class<?> resolveMapperType(Object mapperProxy) {
        if (mapperProxy == null) {
            return null;
        }
        Class<?> proxyClass = mapperProxy.getClass();
        if (proxyClass.isInterface()) {
            return IFrameworkMapper.class.isAssignableFrom(proxyClass) ? proxyClass : null;
        }
        Class<?> best = null;
        for (Class<?> candidate : ClassUtils.getAllInterfaces(mapperProxy)) {
            if (candidate == IFrameworkMapper.class || !IFrameworkMapper.class.isAssignableFrom(candidate)) {
                continue;
            }
            if (best == null || best.isAssignableFrom(candidate)) {
                best = candidate;
            }
        }
        return best;
    }

    /**
     * 从 Mapper 实例直接解析实体类型，等价于 {@code resolveEntityClass(resolveMapperType(mapper))}。
     *
     * @param mapperProxy Mapper 实例
     * @return 实体类型；无法解析时返回 null
     */
    public static Class<?> resolveEntityClassFromProxy(Object mapperProxy) {
        return resolveEntityClass(resolveMapperType(mapperProxy));
    }

    /**
     * 在给定类型变量绑定关系下递归解析实体类型。
     *
     * @param type     待解析的父接口类型
     * @param bindings 外层接口已绑定的类型变量
     * @return 实体类型；无法解析时返回 null
     */
    private static Class<?> resolveEntityClass(Type type, Map<TypeVariable<?>, Type> bindings) {
        if (!(type instanceof ParameterizedType parameterized)) {
            return null;
        }
        Class<?> raw = (Class<?>) parameterized.getRawType();
        TypeVariable<?>[] variables = raw.getTypeParameters();
        Type[] arguments = parameterized.getActualTypeArguments();
        Map<TypeVariable<?>, Type> scope = new HashMap<>(bindings);
        for (int i = 0; i < variables.length && i < arguments.length; i++) {
            Type argument = arguments[i];
            scope.put(variables[i],
                argument instanceof TypeVariable<?> variable && bindings.containsKey(variable) ? bindings.get(variable)
                    : argument);
        }
        if (raw == IFrameworkMapper.class || raw == GenericMapper.class) {
            Type entityType = scope.get(variables[0]);
            return entityType instanceof Class<?> entityClass ? entityClass : null;
        }
        for (Type parent : raw.getGenericInterfaces()) {
            Class<?> entityClass = resolveEntityClass(parent, scope);
            if (entityClass != null) {
                return entityClass;
            }
        }
        return null;
    }
}
