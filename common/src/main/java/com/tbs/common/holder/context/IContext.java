package com.tbs.common.holder.context;

import com.tbs.common.holder.IDictionary;

import java.io.Serializable;

/**
 * 上下文数据接口
 *
 * @author abstergo
 */
public interface IContext extends Serializable {
    /**
     * 获取上下文ID
     *
     * @return
     */
    String getContextId();

    /**
     * 获取上下文数据是否有效
     *
     * @return
     */
    boolean isValid();

    /**
     * 重建上下文，使上下文重新生效
     *
     * @return
     */
    IContext rebuildContext();

    /**
     * 外部提示该上下文可能失效
     */
    void markInvalid();

    /**
     * 获取源数据
     *
     * @return
     */
    IDictionary<String, Object> getSource();


}
