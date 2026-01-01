package com.tbs.common.context;

import com.tbs.common.error.AssertUtil;

/**
 * 环境接口
 *
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

    /**
     * 检查环境是否有效
     */
    default void checkValid() {
        AssertUtil.isTrue(!isValid(), "环境{}已销毁", this);
    }

}
