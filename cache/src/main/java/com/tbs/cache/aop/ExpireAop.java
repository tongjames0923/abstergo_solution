package com.tbs.cache.aop;

import com.tbs.cache.annotation.ExpireOn;
import com.tbs.cache.core.AbstractTtlAwareAdaptCacheManager;
import com.tbs.cache.core.IExpiredActuator;
import com.tbs.common.util.CollUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Aspect
@Component
public class ExpireAop {

    private static final Logger log = LoggerFactory.getLogger(ExpireAop.class);
    @Lazy
    @Resource
    private AbstractTtlAwareAdaptCacheManager cacheManager;

    @Resource
    @Lazy
    private IExpiredActuator expiredActuator;

    @Resource
    CacheAspectSupport cacheAspectSupport;

    @Around(
        "@annotation(com.tbs.cache.annotation.ExpireOn)&&(@annotation(org.springframework.cache.annotation.Cacheable)||@annotation(org.springframework.cache.annotation.CachePut))")
    public Object expire(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        MethodSignature signature = (MethodSignature)pjp.getSignature();
        ExpireOn expireOn = signature.getMethod().getDeclaredAnnotation(ExpireOn.class);
        if (expireOn == null) {
            return result;
        }

        var opts = cacheAspectSupport.getCacheOperationSource()
            .getCacheOperations(signature.getMethod(), pjp.getTarget().getClass());
        if (CollUtil.isEmpty(opts)) {
            return result;
        }

        if (expireOn.value() <= 0) {
            return result;
        }

        String methodName = signature.getMethod().getName();
        String[] explicitCacheNames = expireOn.cacheNames();
        boolean useExplicitNames = !CollUtil.isEmpty(explicitCacheNames);

        // 缓存名称来源优先级：显式声明 > 操作关联 > 全局缓存
        Set<String> cacheNamesSet = new HashSet<>(Math.min(8, opts.size()));

        for (CacheOperation opt : opts) {
            Collection<String> operationNames = opt.getCacheNames();
            if (operationNames == null) {
                continue;
            }

            if (useExplicitNames) {
                operationNames.retainAll(Arrays.asList(explicitCacheNames));
                cacheNamesSet.addAll(operationNames);
            } else {
                cacheNamesSet.addAll(operationNames);
            }
        }

        if (cacheNamesSet.isEmpty()) {
            log.warn("No valid cache names found for method: {}", methodName);
            return result;
        }

        Object key;
        try {
            key = cacheAspectSupport.getKeyGenerator().generate(pjp.getTarget(), signature.getMethod(), pjp.getArgs());
        } catch (Exception e) {
            log.warn("Failed to generate cache key for method: {}, error: {}", methodName, e.getMessage());
            return result;
        }

        long expireMillis = expireOn.timeUnit().toMillis(expireOn.value());
        boolean expireAll = expireOn.expireAll();

        cacheNamesSet.forEach(cacheName -> {
            try {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache == null) {
                    log.warn("Cache not found: {}", cacheName);
                    return;
                }

                AbstractTtlAwareAdaptCacheManager.MethodExpireInfo info =
                    new AbstractTtlAwareAdaptCacheManager.MethodExpireInfo(cacheName, key, expireMillis, expireAll,
                        methodName);
                cacheManager.setMethodExpireDate(info);
            } catch (Exception e) {
                log.error("Failed to set expire for cache: {}, error: {}", cacheName, e.getMessage());
            }
        });

        return result;
    }

}
