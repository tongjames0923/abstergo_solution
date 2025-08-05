package com.tbs.redis.cache;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderExpireActuator;
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
        AssertUtil.isTrue(!(cache instanceof RedisDataHolder), "cache must be RedisDataHolder");
        return ((RedisDataHolder)cache).expire(key.toString(), expireTime);
    }
}
