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
 * <p>
 * 阶段 3 增量：新增 business/version 两个可选过滤维度，供「定向某业务分区/版本」的解析请求使用；
 * 空表示不约束。旧的 {@code of(type)} 与 {@code of(type, beanName)} 保留，存量调用点行为不变。
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

    /**
     * 可选业务分区过滤，空串表示不约束
     */
    private final String business;

    /**
     * 可选发布/灰度版本过滤，空串表示不约束
     */
    private final String version;

    private ObjectResolveContext(Class<T> targetType, String beanName, String business, String version) {
        this.targetType = targetType;
        this.beanName = beanName;
        this.business = business == null ? "" : business;
        this.version = version == null ? "" : version;
    }

    /**
     * 按类型构建解析上下文
     *
     * @param targetType 目标实例类型，不能为null
     * @param <T>        目标实例类型
     * @return 解析上下文
     */
    public static <T> ObjectResolveContext<T> of(Class<T> targetType) {
        return new ObjectResolveContext<>(targetType, null, "", "");
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
        return new ObjectResolveContext<>(targetType, beanName, "", "");
    }

    /**
     * 按类型与业务分区/版本构建解析上下文（不约束 Bean 名称）
     *
     * @param targetType 目标实例类型，不能为null
     * @param business   业务分区过滤，允许为空表示不约束
     * @param version    发布/灰度版本过滤，允许为空表示不约束
     * @param <T>        目标实例类型
     * @return 解析上下文
     */
    public static <T> ObjectResolveContext<T> of(Class<T> targetType, String business, String version) {
        return new ObjectResolveContext<>(targetType, null, business, version);
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

    /**
     * 判断候选来源坐标是否匹配当前解析请求的 business/version 维度
     * <p>
     * 请求侧 business/version 为空表示不约束该维度；非空时要求逐字相等，
     * 用于「定向某业务分区/版本」的解析。来源维度不参与本判定（由调用方按来源定向）。
     * </p>
     *
     * @param coordinate 候选来源坐标，不能为null
     * @return true 表示 business/version 维度命中
     */
    public boolean matchesCoordinate(SourceCoordinate coordinate) {
        return dimensionMatches(business, coordinate.getBusiness())
            && dimensionMatches(version, coordinate.getVersion());
    }

    /**
     * 维度过滤判定：过滤条件为空表示不约束，否则要求逐字相等
     *
     * @param filter 请求侧过滤值，可为空串
     * @param value  候选侧维度值
     * @return true 表示该维度命中
     */
    private boolean dimensionMatches(String filter, String value) {
        return filter == null || filter.isEmpty() || filter.equals(value);
    }
}
