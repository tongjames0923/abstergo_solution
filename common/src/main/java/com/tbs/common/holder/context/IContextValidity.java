package com.tbs.common.holder.context;

/**
 * 上下文有效性接口
 *
 * @author abstergo
 */
public interface IContextValidity {
    /**
     * 获取上下文数据是否有效
     *
     * @return
     */
    boolean isValid();

    /**
     * 外部提示该上下文可能失效
     */
    void markInvalid();

}
