package com.tbs.common.provider;

/**
 * 路由协调器
 * <p>
 * 对贡献者检出的候选实例做最后裁决：返回 true 表示该候选可作为寻址结果。注册中心按贡献者链顺序逐个候选询问，
 * 首个通过即命中；传 {@code null} 等价于全部接受。来源优先级由贡献者链顺序承担，协调器只叠加额外策略
 * （包前缀、灰度名单等）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IRouteCoordinator {

    /**
     * 根据业务线和版本号路由到对应的实现类
     *
     * @param targetClass    目标类
     * @param interfaceClass 接口类
     * @param business       业务线
     * @param version        版本号
     * @return 是否路由成功
     */
    boolean route(Class<?> targetClass, Class<?> interfaceClass, String business, String version);

    /**
     * 缺省路由协调器
     * <p>
     * 不做任何裁决，全部接受：来源优先级已由贡献者链顺序承担。作为 {@link com.tbs.common.provider.annotation.RoutingRef#coordinator()}
     * 的缺省值，具备无参构造，可被装配器直接反射实例化。
     * </p>
     */
    class Default implements IRouteCoordinator {

        /**
         * 裁决候选实例
         *
         * @param targetClass    候选实例类型
         * @param interfaceClass 查询接口
         * @param business       业务分区，空串表示不约束
         * @param version        发布版本，空串表示不约束
         * @return 恒为 true
         */
        @Override
        public boolean route(Class<?> targetClass, Class<?> interfaceClass, String business, String version) {
            return true;
        }
    }
}
