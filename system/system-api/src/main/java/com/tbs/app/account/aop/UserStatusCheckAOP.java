package com.tbs.app.account.aop;

import com.tbs.app.account.IAccountStatusValidator;
import com.tbs.app.account.annotation.UserStatusCheck;
import com.tbs.app.account.annotation.UserStatusChecks;
import com.tbs.app.account.exception.UnNormalAccountStatusException;
import com.tbs.app.account.vo.AccountVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.shared.enums.SystemUserStatusEnum;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户状态检查切面
 * <p>
 * 该切面用于拦截所有标注了 {@link UserStatusCheck} 注解的方法， 在方法执行前进行用户状态验证。
 * </p>
 * <p>
 * 支持以下功能：
 * <ul>
 * <li>自动从方法参数中提取 {@link AccountVO} 对象</li>
 * <li>支持通过 accountKey 属性指定参数名</li>
 * <li>支持多个验证器同时验证</li>
 * <li>支持自定义错误信息</li>
 * <li>支持验证失败时是否抛出异常</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @see UserStatusCheck
 * @see IAccountStatusValidator
 */
@Aspect
@Component
@Slf4j
public class UserStatusCheckAOP {

    private ConcurrentHashMap<String, IAccountStatusValidator> validators = new ConcurrentHashMap<>();

    /**
     * 环绕通知方法
     * <p>
     * 拦截所有标注了 {@link UserStatusCheck} 注解的方法， 在方法执行前进行用户状态验证。
     * </p>
     *
     * @param joinPoint 连接点对象，包含目标方法的相关信息
     * @return 目标方法的执行结果
     * @throws Throwable 方法执行过程中可能抛出的异常
     */
    @Around(
        "@annotation(com.tbs.app.account.annotation.UserStatusCheck) || @annotation(com.tbs.app.account.annotation.UserStatusChecks)||@annotation(com.tbs.app.account.annotation.OnlyActiveAccountUse)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 获取所有 UserStatusCheck 注解（包括通过容器注解的情况）
        List<UserStatusCheck> checks = getUserStatusChecks(method);
        if (checks.isEmpty()) {
            return joinPoint.proceed();
        }

        // 执行所有验证器
        for (UserStatusCheck check : checks) {
            validateAccountStatus(check, method, args);
        }

        return joinPoint.proceed();
    }

    /**
     * 获取方法上的所有 UserStatusCheck 注解
     *
     * @param method 方法对象
     * @return UserStatusCheck 注解列表
     */
    private List<UserStatusCheck> getUserStatusChecks(Method method) {
        List<UserStatusCheck> checks = new ArrayList<>();

        // 直接获取 UserStatusCheck 注解
        UserStatusCheck[] directChecks = method.getAnnotationsByType(UserStatusCheck.class);
        if (directChecks != null) {
            checks.addAll(Arrays.asList(directChecks));
        }

        // 获取 UserStatusChecks 容器注解中的所有 UserStatusCheck
        UserStatusChecks containerChecks = AnnotatedElementUtils.findMergedAnnotation(method, UserStatusChecks.class);
        if (containerChecks != null) {
            checks.addAll(Arrays.asList(containerChecks.value()));
        }

        // 递归检查元注解，查找包含 UserStatusCheck 的组合注解
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(UserStatusCheck.class)) {
                UserStatusCheck mergedCheck =
                    AnnotatedElementUtils.findMergedAnnotation(annotation.annotationType(), UserStatusCheck.class);
                if (mergedCheck != null) {
                    checks.add(mergedCheck);
                }
            }
        }

        return checks;
    }

    /**
     * 验证账户状态
     *
     * @param check  UserStatusCheck 注解
     * @param method 方法对象
     * @param args   方法参数
     */
    private void validateAccountStatus(UserStatusCheck check, Method method, Object[] args) {
        Class<? extends IAccountStatusValidator> validatorClass = check.validator();
        if (validatorClass == null || IAccountStatusValidator.class.equals(validatorClass)) {
            log.warn("UserStatusCheck 注解未指定有效的验证器类");
            return;
        }

        // 获取验证器实例
        IAccountStatusValidator validator = null;

        try {
            validator = validators.computeIfAbsent(validatorClass.getName(), (key) -> {
                try {
                    return validatorClass.newInstance();
                } catch (Exception e) {
                    log.warn("无法创建验证器实例: {}", validatorClass.getName(), e);
                    return null;
                }
            });
        } catch (Exception e) {
            log.warn("无法创建验证器实例: {}", validatorClass.getName(), e);
        }

        if (validator == null) {
            log.warn("无法获取验证器实例: {}", validatorClass.getName());
            return;
        }

        // 提取 AccountVO 对象
        AccountVO accountVO = extractAccountVO(check, method, args);
        if (accountVO == null) {
            log.debug("方法参数中未找到 AccountVO 对象");
            return;
        }

        try {
            // 执行验证
            validator.validate(accountVO);
        } catch (UnNormalAccountStatusException e) {
            AssertUtil.throwError(() -> e);
        } catch (Exception e) {
            log.warn("验证账户状态时发生异常", e);
        }
    }

    /**
     * 从方法参数中提取 AccountVO 对象
     *
     * @param check  UserStatusCheck 注解
     * @param method 方法对象
     * @param args   方法参数
     * @return AccountVO 对象，如果未找到则返回 null
     */
    private AccountVO extractAccountVO(UserStatusCheck check, Method method, Object[] args) {
        var u = AuthUtil.getInstance().getCurrentUser();
        AssertUtil.isTrue(u == null).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).message("用户未登录").throwIt();
        AccountVO accountVO = new AccountVO();
        accountVO.setUserName(u.getUsername());
        int flag = SystemUserStatusEnum.ACTIVE.getCode();
        if (!u.isAccountNonLocked()) {
            flag = SystemUserStatusEnum.LOCKED.getCode();
        }
        if (!u.isEnabled()) {
            flag = SystemUserStatusEnum.INACTIVE.getCode();
        }
        accountVO.setFlag(flag);
        return accountVO;
    }

}
