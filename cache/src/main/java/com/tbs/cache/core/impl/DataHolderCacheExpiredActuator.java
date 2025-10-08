package com.tbs.cache.core.impl;

import com.tbs.cache.core.expire.ICacheExpiredActuator;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderExpireActuator;
import com.tbs.common.data.impl.LocalDataExpireActuator;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.FeatureWrapperHelper;
import com.tbs.common.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;

import java.util.Date;

@Slf4j
public class DataHolderCacheExpiredActuator implements ICacheExpiredActuator {
    @Override
    public boolean expireAll(Cache cache, Date expireTime) {
        return expire(cache, "*", expireTime);
    }

    @Override
    public boolean expire(@NotNull Cache cache, Object key, @NotNull Date expireTime) {
        var holder = FeatureWrapperHelper.unwrap(cache, IDataHolder.class);
        AssertUtil.isTrue(holder.isEmpty(),
            () -> new UnsupportedOperationException("不支持非数据持有器（DataHolder类型的）缓存"));
        var expiredActuator = FeatureWrapperHelper.unwrap(cache, IDataHolderExpireActuator.class);
        if (expiredActuator.isEmpty()) {
            try {
                return SpringUtil.getBean(LocalDataExpireActuator.class).expire(holder.get(), key, expireTime);
            } catch (Exception e) {
                log.error("获取缓存数据过期处理失败", e);
                return false;
            }
        }
        return expiredActuator.get().expire(holder.get(), key, expireTime);
    }
}
