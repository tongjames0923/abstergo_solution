package com.tbs.web.trace.aop;

import com.tbs.web.trace.core.AbstractTraceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import javax.annotation.Resource;

/**
 * 方法调用链追踪
 *
 * @author abstergo
 */
@Aspect
public class MethodTraceAop {

    @Resource
    private AbstractTraceContext traceContext;

    @Around("@annotation(com.tbs.web.trace.annotation.Traced)")
    public Object traceAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        String parentTraceId = traceContext.getCurrentMethodTraceObject().getTraceId();
        traceContext.beginMethod(parentTraceId, signature.getMethod(),
            joinPoint.getArgs(), joinPoint.getTarget());
        try {
            return joinPoint.proceed();
        } finally {
            String currentTraceId = traceContext.getCurrentMethodTraceObject().getTraceId();
            traceContext.endMethod(currentTraceId);
        }
    }
}
