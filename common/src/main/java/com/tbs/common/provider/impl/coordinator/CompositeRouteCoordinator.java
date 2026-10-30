package com.tbs.common.provider.impl.coordinator;

import com.tbs.common.provider.IRouteCoordinator;

import java.util.List;

/**
 * 组合路由协调器
 * <p>
 * 把多个协调器按「全部通过」或「任一通过」组合，便于叠加策略：例如先限定包前缀、再叠加业务自定义裁决。
 * 空组合时「全部通过」恒为 true、「任一通过」恒为 false，与集合语义一致。
 * </p>
 * <p>
 * 不注册为 Bean：组合内容取决于调用点的策略，由调用方构造。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class CompositeRouteCoordinator implements IRouteCoordinator {

    /**
     * 是否要求全部协调器通过：true 为「全部」，false 为「任一」
     */
    private final boolean requireAll;

    /**
     * 被组合的协调器列表
     */
    private final List<IRouteCoordinator> coordinators;

    /**
     * 构造组合协调器
     *
     * @param requireAll   是否要求全部通过
     * @param coordinators 被组合的协调器列表，不能为null
     */
    private CompositeRouteCoordinator(boolean requireAll, List<IRouteCoordinator> coordinators) {
        this.requireAll = requireAll;
        this.coordinators = coordinators;
    }

    /**
     * 组合为「全部通过」
     *
     * @param coordinators 被组合的协调器，不能为null
     * @return 组合协调器
     */
    public static CompositeRouteCoordinator all(IRouteCoordinator... coordinators) {
        return new CompositeRouteCoordinator(true, List.of(coordinators));
    }

    /**
     * 组合为「任一通过」
     *
     * @param coordinators 被组合的协调器，不能为null
     * @return 组合协调器
     */
    public static CompositeRouteCoordinator any(IRouteCoordinator... coordinators) {
        return new CompositeRouteCoordinator(false, List.of(coordinators));
    }

    /**
     * 裁决候选实例
     *
     * @param targetClass    候选实例类型
     * @param interfaceClass 查询接口
     * @param business       业务分区，空串表示不约束
     * @param version        发布版本，空串表示不约束
     * @return 组合后的裁决结果
     */
    @Override
    public boolean route(Class<?> targetClass, Class<?> interfaceClass, String business, String version) {
        if (requireAll) {
            return coordinators.stream()
                .allMatch(coordinator -> coordinator.route(targetClass, interfaceClass, business, version));
        }
        return coordinators.stream()
            .anyMatch(coordinator -> coordinator.route(targetClass, interfaceClass, business, version));
    }
}
