package com.tbs.common.holder.context;

/**
 * 上下文数据接口
 *
 * @author abstergo
 */
public interface IContext<V> extends IContextIdentity, IContextValidity, IContextRebuildable<V> {

}
