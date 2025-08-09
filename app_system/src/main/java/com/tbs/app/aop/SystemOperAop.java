package com.tbs.app.aop;

import com.alibaba.fastjson2.JSON;
import com.tbs.app.config.SystemAppProperty;
import com.tbs.app.constant.SystemOperateLogConstant;
import com.tbs.app.pojo.entity.SystemOperateRecordEntity;
import com.tbs.app.util.NetUtil;
import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.event.IEvent;
import com.tbs.broadcast.event.impl.SimpleEvent;
import com.tbs.broadcast.publisher.IPublisher;
import com.tbs.common.delegate.IDelegateTemplate;
import com.tbs.web.BaseController;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.util.SerializationUtils;

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
    private IPublisher publisher;

    @Resource
    private SystemAppProperty appProperty;

    @Resource
    private IDelegateTemplate delegateTemplate;

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
            IEvent event = createRecordItem(joinPoint, (BaseController)target, begin, end, result);
            HandleMode handleMode = appProperty.isLogAsync() ? HandleMode.SYNC : HandleMode.ASYNC;
            publisher.publish(event, handleMode);
        } catch (Throwable e) {
            log.error("record log error", e);
        }

        return result;
    }

    private IEvent createRecordItem(ProceedingJoinPoint joinPoint, BaseController target, Date begin, Date end,
        Object result) {
        BaseController baseController = target;
        SimpleEvent event = new SimpleEvent(SystemOperateLogConstant.SYSTEM_OPERATE_LOG_EVENT_ID, "");
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
        event.setEventData(SerializationUtils.serialize(record), SystemOperateRecordEntity.class);
        return event;
    }

}
