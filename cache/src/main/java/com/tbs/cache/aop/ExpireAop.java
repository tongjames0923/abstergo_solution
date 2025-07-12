package com.tbs.cache.aop;

import com.tbs.cache.annotation.ExpireOn;
import com.tbs.cache.core.AbstractTtlAwareAdaptCacheManager;
import com.tbs.cache.core.IExpiredActuator;
import com.tbs.common.util.CollUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            List<String> cacheNames = new ArrayList<>(8);

            if (CollUtil.isEmpty(expireOn.cacheNames())) {
                cacheNames.addAll(cacheManager.getCacheNames());
            } else {
                cacheNames.addAll(Arrays.asList(expireOn.cacheNames()));
            }
            cacheNames.forEach(cacheName -> {
                AbstractTtlAwareAdaptCacheManager.MethodExpireInfo info =
                    new AbstractTtlAwareAdaptCacheManager.MethodExpireInfo(cacheName, key,
                        expireOn.timeUnit().toMillis(expireOn.value()), expireOn.expireAll());
                cacheManager.setMethodExpireDate(info);
            });

        }
        return result;
    }
}
