package com.tbs.redis.cache;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderExpireActuator;
import com.tbs.common.data.IDataHolderFeatureWrapper;
import com.tbs.common.error.AssertUtil;
import com.tbs.redis.holder.RedisDataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * RedisDataHolderExpireActuator
 *
 * @author tongj
 */
public class RedisDataHolderExpireActuator implements IDataHolderExpireActuator {

    @Override
    public boolean expire(@NotNull IDataHolder cache, Object key, @NotNull Date expireTime) {
        if (cache instanceof IDataHolderFeatureWrapper) {
            IDataHolderFeatureWrapper wrapper = (IDataHolderFeatureWrapper)cache;
            cache = wrapper.getOrginal();
            key = wrapper.reKey(key.toString());
        }
        if (cache instanceof RedisDataHolder) {
            return ((RedisDataHolder)cache).expire(key.toString(), expireTime);
        }
        AssertUtil.throwError(() -> new UnsupportedOperationException("cache is not RedisDataHolder"));
        return false;
    }

}
