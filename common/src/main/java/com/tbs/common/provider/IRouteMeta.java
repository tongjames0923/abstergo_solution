package com.tbs.common.provider;

import com.tbs.common.provider.vo.InstanceMeta;

/**
 * 路由元信息接口
 */
public interface IRouteMeta {
    /**
     * 获取元信息
     */
    default InstanceMeta getMeta() {
        return new InstanceMeta();
    }
}
