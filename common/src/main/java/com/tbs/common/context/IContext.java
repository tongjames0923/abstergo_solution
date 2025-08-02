package com.tbs.common.context;

/**
 * 环境接口
 * @author abstergo
 */
public interface IContext {
    /**
     * 销毁环境，意味着上下文不可用
     */
    void destroy();

    /**
     * 重建环境，意味着上下文可用
     */
    void rebuild();

    /**
     * 环境是否有效
     *
     * @return
     */
    boolean isValid();
}
