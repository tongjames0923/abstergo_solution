package com.tbs.common.holder;

import com.tbs.common.holder.context.IContext;

/**
 * 上下文数据存储接口
 *
 * @author abstergo
 */
public interface IContextHolder {

    /**
     * 根据上下文ID获取上下文
     *
     * @param contextId
     * @return
     */
    IContext getContextByContextId(String contextId);
    
}
