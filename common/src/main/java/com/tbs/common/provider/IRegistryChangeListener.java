package com.tbs.common.provider;

/**
 * 注册条目变更监听器。
 * <p>
 * 动态来源经 {@link IRegistryCenter#replaceEntriesBySource} 增量刷新后回调，供代理缓存失效、健康状态联动、
 * 观测埋点等使用。监听器应保持轻量且不得抛出，异常由中心吞掉并记录，避免影响条目刷新主流程。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IRegistryChangeListener {

    /**
     * 某来源坐标下的条目发生变化
     *
     * @param coordinate 发生变化的来源坐标，不能为null
     * @param key        发生变化的接口类型，不能为null
     */
    void onEntriesChanged(SourceCoordinate coordinate, Class<?> key);
}
