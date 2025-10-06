package com.tbs.common.data;

import java.util.Date;

public interface IDataHolderExpireSupport {

    /**
     * 缓存过期处理
     *
     * @param cache
     * @param key
     * @param expireTime
     * @return
     */
    boolean expire(Object key, Date expireTime);

    /**
     * 缓存过期处理
     *
     * @param cache
     * @param expireTime
     */
    boolean expireAll(Date expireTime);
}
