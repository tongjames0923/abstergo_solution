package com.tbs.web.trace.aop;

import com.tbs.web.trace.core.AbstractTraceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 方法调用链追踪
 *
 * @author abstergo
 */
@Aspect
@Component
public class MethodTraceAop {

    @Resource
    private AbstractTraceContext traceContext;

    @Around("@annotation(com.tbs.web.trace.annotation.Traced)")
    public Object traceAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object res = null;
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        traceContext.beginMethod(traceContext.getCurrentMethodTraceObject().getTraceId(), signature.getMethod(),
            joinPoint.getArgs(), joinPoint.getTarget());
        try {
            res = joinPoint.proceed();
        } finally {
            traceContext.endMethod(traceContext.getCurrentMethodTraceObject().getTraceId());
        }
        return res;
    }
}
