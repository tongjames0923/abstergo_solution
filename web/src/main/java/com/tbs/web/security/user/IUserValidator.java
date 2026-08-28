package com.tbs.web.security.user;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.framework.SpringUtil;
import org.springframework.core.Ordered;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 用户验证接口 用于验证用户
 * <p>
 * 在认证过程中对用户详情做附加校验（如账号状态、绑定关系、风控规则等）。
 * 通过 Spring 容器收集所有实现并按 {@link Ordered} 顺序串联执行。
 * </p>
 * @author abstergo
 * @see Ordered 决定验证器执行顺序
 */
public interface IUserValidator extends Ordered {

    /**
     * 获取 Spring 容器管理的全部用户验证器
     * <p>
     * 按 {@link #getOrder()} 升序排序后返回，便于调用方依次执行校验链。
     * </p>
     *
     * @return 已按序排列的用户验证器集合，无实现时返回空集合
     */
    public static Collection<IUserValidator> getSpringManagedValidators() {
        return SpringUtil.getBeansOfType(IUserValidator.class).values().stream()
            .sorted((a1, a2) -> a1.getOrder() - a2.getOrder()).collect(Collectors.toList());
    }

    /**
     * 验证用户信息的有效性
     * <p>
     * 校验不通过时应抛出携带国际化信息的 {@link FormattedRuntimeException}，由上层认证流程转为相应响应。
     * </p>
     *
     * @param userDetails 待校验的用户详情对象，不能为空
     * @throws FormattedRuntimeException 当用户验证失败时抛出，携带业务可读的错误信息
     */
    void validate(UserDetails userDetails) throws FormattedRuntimeException;

}
