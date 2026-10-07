package com.tbs.common.provider;

import lombok.Getter;
import lombok.ToString;

import com.tbs.common.constant.ProviderConstant;

/**
 * 对象实例解析结果载体
 * <p>
 * 封装一次实例解析的完整结果：实例本体、来源元数据与实例标识。
 * 实例标识在不同来源下含义不同：Spring 来源为 Bean 名称，Dubbo 来源为提供者地址（host:port），
 * Dubbo 整体负载均衡引用为固定标识（见 {@link #isClusterReference()}）。
 * 通过来源与实例标识，调用方可以在多实现枚举结果中区分并定向选择指定实现。
 * </p>
 *
 * @param <T> 实例类型
 * @author abstergo
 * @since 1.0.0
 */
@Getter
@ToString
public class ResolvedObject<T> {

    /**
     * 实例本体
     */
    private final T instance;

    /**
     * 实例来源
     */
    private final ObjectSource source;

    /**
     * 实例标识：Spring 来源为 Bean 名称，Dubbo 来源为提供者地址（host:port）或整体引用标识
     */
    private final String instanceKey;

    private ResolvedObject(T instance, ObjectSource source, String instanceKey) {
        this.instance = instance;
        this.source = source;
        this.instanceKey = instanceKey;
    }

    /**
     * 构建解析结果
     *
     * @param instance    实例本体，不能为null
     * @param source      实例来源，不能为null
     * @param instanceKey 实例标识，不能为null
     * @param <T>         实例类型
     * @return 解析结果载体
     */
    public static <T> ResolvedObject<T> of(T instance, ObjectSource source, String instanceKey) {
        return new ResolvedObject<>(instance, source, instanceKey);
    }

    /**
     * 判断当前结果是否来自指定来源
     *
     * @param source 待比较的来源，不能为null
     * @return true 表示来源一致
     */
    public boolean isSource(ObjectSource source) {
        return this.source == source;
    }

    /**
     * 判断当前结果是否为 Dubbo 整体负载均衡引用
     * <p>
     * 整体引用由 Dubbo 自行在集群实例间负载均衡，不对应任何固定提供者地址；
     * 与逐实例直连引用的区分只需实例标识，无需调用方硬编码标识值。
     * </p>
     *
     * @return true 表示当前结果为 Dubbo 来源且实例标识为整体引用标识
     */
    public boolean isClusterReference() {
        return source.isDubbo() && ProviderConstant.DUBBO_CLUSTER_INSTANCE_KEY.equals(instanceKey);
    }
}
