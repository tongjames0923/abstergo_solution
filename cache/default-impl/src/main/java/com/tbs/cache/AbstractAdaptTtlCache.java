package com.tbs.cache;

import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.common.util.SpringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.cache.Cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public abstract class AbstractAdaptTtlCache extends AbstractTtlCache {
    private Cache orginCache;
    private List<ICacheListenner> listeners = null;

    private AtomicReference<ExpireDetails> expireAll = new AtomicReference<>(null);
    private ConcurrentHashMap<Object, ExpireDetails> expireMap = new ConcurrentHashMap<>(32);

    public AbstractAdaptTtlCache(Cache orginCache) {
        this.orginCache = orginCache;
    }

    @Override
    protected List<ICacheListenner> getListeners() {
        if (listeners == null) {
            synchronized (this) {
                if (listeners == null) {
                    try {
                        listeners = SpringUtil.getBeansOfType(ICacheListenner.class).values().stream()
                            .collect(Collectors.toList());

                    } catch (Exception e) {
                        listeners = new ArrayList<>();
                    }
                }
            }
        }
        return listeners;
    }

    @Override
    public void callToExpire(Object key, Date expireTime) {
        expireMap.put(key, new ExpireDetails(expireTime, false, key));
    }

    @Override
    public void callToExpireAll(Date expireTime) {
        expireAll.set(new ExpireDetails(expireTime, true, null));
    }

    @Override
    protected Optional<Date> isCallToExpire(Object key) {
        return Optional.ofNullable(expireMap.remove(key)).map(expireDetails -> {
            if (expireDetails.isAll()) {
                return null;
            } else {
                return expireDetails.getExpireTime();
            }
        });
    }

    @Override
    protected Optional<Date> isCallToExpireAll() {
        return Optional.ofNullable(expireAll.get()).map(expireDetails -> expireDetails.getExpireTime());
    }

    @Override
    public Cache getOrginal() {
        return orginCache;
    }

    @Override
    public void setOrginal(Cache orginal) {
        this.orginCache = orginal;
    }

    @Data
    @AllArgsConstructor
    private static class ExpireDetails {
        private Date expireTime;
        private boolean isAll;
        private Object key;
    }

}
