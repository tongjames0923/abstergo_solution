package com.tbs.app.aop;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.alibaba.fastjson2.JSON;
import com.tbs.app.config.SystemAppProperty;
import com.tbs.app.pojo.entity.SystemOperateRecordEntity;
import com.tbs.app.service.log.ISystemOperateLog;
import com.tbs.app.util.NetUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.BaseController;
import com.tbs.web.uitl.AuthUtil;

import lombok.extern.slf4j.Slf4j;

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

	@Pointcut("within(com.tbs.web.BaseController+) && (@annotation(org.springframework.web.bind.annotation.RequestMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.GetMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.PutMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.PostMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.DeleteMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.PatchMapping))")
	public void pointcut() {
	}

	@Resource
	private HttpServletRequest request;

	@Around("pointcut()")
	Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
		Date begin = new Date();
		Object result = null;
		Throwable businessError = null;
		try {
			result = joinPoint.proceed();
		} catch (Throwable e) {
			businessError = e;
		}

		Date end = new Date();
		Object target = joinPoint.getTarget();
		if (!(target instanceof BaseController)) {
			log.warn("{} is not a BaseController,will not record log", target.getClass().getName());
			return result;
		}
		try {
			SpringUtil.getBeanSafe(ISystemOperateLog.class, throwable -> log.error("记录操作日志错误", throwable))
					.log(createRecordItem(joinPoint, (BaseController) target, begin, end, result, businessError));
		} catch (Throwable e) {
			log.error("record log error", e);
		}
		if (businessError != null) {
			throw businessError;
		}
		return result;
	}

	private SystemOperateRecordEntity createRecordItem(ProceedingJoinPoint joinPoint, BaseController target, Date begin,
			Date end, Object result, Throwable error) {
		BaseController baseController = target;
		SystemOperateRecordEntity record = new SystemOperateRecordEntity();
		record.setOperateType(
				joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
		record.setCostTime(end.getTime() - begin.getTime());
		record.setOperateIp(NetUtil.getIp(request));
		record.setOperateTime(begin);
		record.setMethod(request.getMethod());
		record.setOperateUrl(request.getRequestURI());
		if (error != null) {
			record.setOperateError(JSON.toJSONString(error));
		}
		record.setOperateContent(baseController.getBusinessName());
		if (AuthUtil.getInstance().isUserLoggedIn()) {
			record.setOperateUserName(AuthUtil.getInstance().getCurrentUser().getUsername());
		} else {
			record.setOperateUserName("NULL USERNAME:" + NetUtil.getIp(request));
		}
		record.setOperateParamJson(JSON.toJSONString(joinPoint.getArgs()));
		record.setOperateResultJson(JSON.toJSONString(result));
		return record;
	}

}
