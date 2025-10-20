package com.tbs.cache.limit;

import java.util.Date;

public interface ICacheOperationView {
    /**
     * 获取缓存key
     *
     * @return
     */
    String getKey();

    /**
     * 获取缓存存入次数
     *
     * @return
     */
    int getPutCount();

    /**
     * 获取缓存获取次数
     *
     * @return
     */
    int getGetCount();

    /**
     * 获取缓存删除次数
     *
     * @return
     */
    int getRemoveCount();

    /**
     * 获取缓存最后存入时间
     *
     * @return
     */
    Date getLastPutTime();

    /**
     * 获取缓存最后获取时间
     *
     * @return
     */
    Date getLastGetTime();

    /**
     * 获取缓存最后删除时间
     *
     * @return
     */
    Date getLastRemoveTime();
}
