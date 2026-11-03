package com.tbs.cache;

import org.springframework.cache.Cache;

import java.util.List;

public interface IMultiLevelSupport {
    /**
     * 获取多级缓存列表
     *
     * @return
     */
    List<Cache> getLevels();
}
