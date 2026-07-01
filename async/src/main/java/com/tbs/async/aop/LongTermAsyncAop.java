package com.tbs.async.aop;

import com.tbs.async.annotation.LongTermAsync;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 长期异步切面
 * <p>
 * 设计初衷：为需要长期在后台运行的操作提供独立执行环境，
 * 避免使用线程池导致线程资源被长期占用，影响其他任务调度。
 * </p>
 *
 * @author abstergo
 */
@Aspect
@Component
@Slf4j
public class LongTermAsyncAop {

    private static final List<Thread> BACKGROUND_THREADS = new CopyOnWriteArrayList<>();

    @Around("@annotation(com.tbs.async.annotation.LongTermAsync)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        LongTermAsync longTermAsync = methodSignature.getMethod().getDeclaredAnnotation(LongTermAsync.class);
        if (longTermAsync == null) {
            return point.proceed();
        }

        String threadName = StrUtil.isNotEmpty(longTermAsync.value()) ? longTermAsync.value()
            : methodSignature.getDeclaringTypeName() + "-" + methodSignature.getName();

        log.debug("start one long term thread {} for {}", threadName, methodSignature.toLongString());
        Thread thread = new Thread(() -> {
            try {
                point.proceed();
            } catch (Throwable throwable) {
                SpringUtil.getBeanSafe(longTermAsync.onThreadError(), (e) -> {
                    log.error("线程执行错误.", e);
                }).onThreadError(throwable, Thread.currentThread());
            } finally {
                BACKGROUND_THREADS.remove(Thread.currentThread());
            }
        }, threadName);
        BACKGROUND_THREADS.add(thread);
        thread.start();
        return null;
    }
}
