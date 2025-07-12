package com.tbs.cache.aop;

import com.tbs.cache.annotation.ExpireOn;
import com.tbs.cache.constant.CacheNameConstant;
import com.tbs.cache.core.AbstractTtlAwareAdaptCacheManager;
import com.tbs.cache.core.IExpiredActuator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Aspect
@Component
public class ExpireAop {

    @Lazy
    @Resource
    private AbstractTtlAwareAdaptCacheManager cacheManager;

    @Resource
    @Lazy
    private IExpiredActuator expiredActuator;

    @Resource
    CacheAspectSupport cacheAspectSupport;

    @Around("@annotation(com.tbs.cache.annotation.ExpireOn)")
    public Object expire(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        MethodSignature signature = (MethodSignature)pjp.getSignature();
        ExpireOn expireOn = signature.getMethod().getDeclaredAnnotation(ExpireOn.class);
        if (expireOn == null) {
            return result;
        }
        if (expireOn.value() > 0) {
            Object key =
                cacheAspectSupport.getKeyGenerator().generate(pjp.getTarget(), signature.getMethod(), pjp.getArgs());
            String cacheName = CacheNameConstant.TTL_CACHE;
            AbstractTtlAwareAdaptCacheManager.MethodExpireInfo info =
                new AbstractTtlAwareAdaptCacheManager.MethodExpireInfo(cacheName, key,
                    expireOn.timeUnit().toMillis(expireOn.value()), expireOn.expireAll());
            cacheManager.setMethodExpireDate(info);
        }
        return result;
    }
}
