package com.tbs.app.aop;

import com.alibaba.fastjson2.JSON;
import com.tbs.app.config.SystemAppProperty;
import com.tbs.app.pojo.entity.SystemOperateRecordEntity;
import com.tbs.app.service.log.ISystemOperateLog;
import com.tbs.app.util.NetUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.BaseController;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 系统操作记录
 *
 * @author tongj
 */
@Aspect
@Slf4j
public class SystemOperAop {

    @Resource
    private SystemAppProperty appProperty;

    @Pointcut(
        "within(com.tbs.web.BaseController+) && (@annotation(org.springframework.web.bind.annotation.RequestMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.GetMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.PutMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.PatchMapping))")
    public void pointcut() {
    }

    @Resource
    private HttpServletRequest request;

    @Around("pointcut()")
    Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Date begin = new Date();
        Object result = joinPoint.proceed();
        Date end = new Date();
        Object target = joinPoint.getTarget();
        if (!(target instanceof BaseController)) {
            log.warn("{} is not a BaseController,will not record log", target.getClass().getName());
            return result;
        }
        try {
            SpringUtil.getBeanSafe(ISystemOperateLog.class, throwable -> log.error("记录操作日志错误", throwable))
                .log(createRecordItem(joinPoint, (BaseController)target, begin, end, result));
        } catch (Throwable e) {
            log.error("record log error", e);
        }

        return result;
    }

    private SystemOperateRecordEntity createRecordItem(ProceedingJoinPoint joinPoint, BaseController target, Date begin,
        Date end, Object result) {
        BaseController baseController = target;
        SystemOperateRecordEntity record = new SystemOperateRecordEntity();
        record.setOperateType(
            joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        record.setCostTime(end.getTime() - begin.getTime());
        record.setOperateIp(NetUtil.getIp(request));
        record.setOperateTime(begin);
        record.setMethod(request.getMethod());
        record.setOperateUrl(request.getRequestURI());
        record.setOperateContent(baseController.getBusinessName());
        if (AuthUtil.getInstance().isUserLoggedIn()) {
            record.setOperateUserName(AuthUtil.getInstance().getCurrentUser().getUsername());
        }
        record.setOperateParamJson(JSON.toJSONString(joinPoint.getArgs()));
        record.setOperateResultJson(JSON.toJSONString(result));
        return record;
    }

}
