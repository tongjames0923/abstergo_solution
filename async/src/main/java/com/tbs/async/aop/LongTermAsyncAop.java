package com.tbs.async.aop;

import com.tbs.async.annotation.LongTermAsync;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.SpringUtil;
import com.tbs.common.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

/**
 * 长期异步切面
 *
 * @author abstergo
 */
@Aspect
@Component
@Slf4j
public class LongTermAsyncAop {

    @Around("@annotation(com.tbs.async.annotation.LongTermAsync)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature methodSignature = (MethodSignature)point.getSignature();
        if (methodSignature.getReturnType() != void.class) {
            throw new FormattedRuntimeException("长期异步函数无返回值函数");
        }
        LongTermAsync longTermAsync = methodSignature.getMethod().getDeclaredAnnotation(LongTermAsync.class);
        if (longTermAsync == null) {
            return point.proceed();
        }

        String threadName = StrUtil.isNotEmpty(longTermAsync.value()) ? longTermAsync.value()
            : methodSignature.getDeclaringTypeName() + "-" + methodSignature.getName();
        Object[] r = new Object[1];
        log.debug("start one long term thread {} for {}", threadName, methodSignature.toLongString());
        Executors.newSingleThreadExecutor(new CustomizableThreadFactory(threadName)).execute(() -> {
            try {
                r[0] = point.proceed();
            } catch (Throwable throwable) {
                SpringUtil.getBeanSafe(longTermAsync.onThreadError(), (e) -> {
                    log.error("线程执行错误.", e);
                });
            }
        });
        return r[0];
    }

}
