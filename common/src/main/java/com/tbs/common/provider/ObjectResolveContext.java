package com.tbs.common.provider;

import lombok.Getter;
import lombok.ToString;

/**
 * 对象实例解析上下文
 * <p>
 * 解析器 SPI 的入参载体，描述一次实例解析请求的目标类型与可选实例标识。
 * 实例标识在各来源下语义统一：Spring 来源为 Bean 名称，Dubbo 来源为提供者地址（host:port），
 * 为空时表示按类型解析，指定时必须由解析器按自身实例标识精确匹配，不允许降级返回其他实例。
 * </p>
 *
 * @param <T> 目标实例类型
 * @author abstergo
 * @since 1.0.0
 */
@Getter
@ToString
public class ObjectResolveContext<T> {

    /**
     * 目标实例类型，不能为null
     */
    private final Class<T> targetType;

    /**
     * 可选的精确 Bean 名称，为空表示按类型解析
     */
    private final String beanName;

    private ObjectResolveContext(Class<T> targetType, String beanName) {
        this.targetType = targetType;
        this.beanName = beanName;
    }

    /**
     * 按类型构建解析上下文
     *
     * @param targetType 目标实例类型，不能为null
     * @param <T>        目标实例类型
     * @return 解析上下文
     */
    public static <T> ObjectResolveContext<T> of(Class<T> targetType) {
        return of(targetType, null);
    }

    /**
     * 按类型与 Bean 名称构建解析上下文
     *
     * @param targetType 目标实例类型，不能为null
     * @param beanName   精确 Bean 名称，允许为空
     * @param <T>        目标实例类型
     * @return 解析上下文
     */
    public static <T> ObjectResolveContext<T> of(Class<T> targetType, String beanName) {
        return new ObjectResolveContext<>(targetType, beanName);
    }

    /**
     * 判断是否指定了精确实例标识
     *
     * @return true 表示已指定实例标识
     */
    public boolean hasBeanName() {
        return beanName != null && !beanName.isEmpty();
    }

    /**
     * 判断候选实例标识是否匹配当前解析请求
     * <p>
     * 未指定实例标识时视为全部匹配（按类型解析）；已指定时要求完全一致，
     * 避免调用方请求「精确某个实现」却被解析到其他来源的任意实例。
     * </p>
     *
     * @param instanceKey 候选实例标识，允许为null
     * @return true 表示该候选实例可纳入解析结果
     */
    public boolean matchesInstanceKey(String instanceKey) {
        return !hasBeanName() || beanName.equals(instanceKey);
    }
}
