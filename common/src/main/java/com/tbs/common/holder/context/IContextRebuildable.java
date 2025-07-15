package com.tbs.common.holder.context;

/**
 * 上下文重建接口
 *
 * @author abstergo
 * @param <V>
 */
public interface IContextRebuildable<V> {
    /**
     * 重建上下文，使上下文重新生效
     *
     * @return
     */
    IContext<V> rebuildContext();

}
