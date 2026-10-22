package com.tbs.common.provider;

/**
 * 远程来源目标解析器契约。
 * <p>
 * 与 {@link IRegistrationSource}（把来源实例逐条枚举进注册中心）相对：本契约面向「不做逐实例枚举、
 * 由远程中间件自身完成服务发现与负载均衡」的来源。路由代理按 {@link RoutingStrategy#preference()}
 * 的偏好遍历来源时，命中本解析器登记的来源即向其索取一个绑定 business/version 的远程目标，
 * 该目标的存在性与可用性由远程框架在调用期保障，注册中心内不再有对应的枚举条目。
 * </p>
 * <p>
 * Dubbo 3 应用级发现即采用此形态：给定接口与 business/version 直接构造 Dubbo 原生集群引用，
 * 由 Dubbo 依据注册中心完成发现与容错，从而不依赖 provider 的注册模式（接口级/应用级/双注册）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IRemoteTargetResolver {

    /**
     * 本解析器负责的远程来源
     *
     * @return 来源标识，不能为null
     */
    ObjectSource source();

    /**
     * 按接口与定向维度解析一个远程调用目标
     * <p>
     * 目标存在性不要求此刻一定有空闲提供者：来源框架（如 Dubbo）在调用期做服务发现与容错，
     * 无可用提供者时于此处返回的引用会在实际调用时抛出框架级异常。
     * 当来源未配置或不可用时返回 null，交由路由代理降级到下一优先来源或走无提供者处理。
     * </p>
     *
     * @param interfaceType 目标接口类型，不能为null
     * @param business      定向业务分区，null/空表示不约束（匹配无分组默认提供者）
     * @param version       定向发布版本，null/空表示不约束
     * @param <T>           目标接口类型
     * @return 远程调用目标实例；来源不可用时为 null
     */
    <T> T resolveTarget(Class<T> interfaceType, String business, String version);
}
