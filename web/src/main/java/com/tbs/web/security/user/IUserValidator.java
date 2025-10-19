package com.tbs.web.security.user;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.framework.SpringUtil;
import org.springframework.core.Ordered;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 用户验证接口 用于验证用户
 * @author abstergo
 */
public interface IUserValidator extends Ordered {

    /**
     * 获取Spring管理的所有用户验证接口
     *
     * @return 所有用户验证接口
     */
    public static Collection<IUserValidator> getSpringManagedValidators() {
        return SpringUtil.getBeansOfType(IUserValidator.class).values().stream()
            .sorted((a1, a2) -> a1.getOrder() - a2.getOrder()).collect(Collectors.toList());
    }

    /**
     * 验证用户信息的有效性
     *
     * @param userDetails 包含用户详细信息的对象，用于验证用户身份和权限
     * @throws FormattedRuntimeException 当用户验证失败时抛出格式化运行时异常
     */
    void validate(UserDetails userDetails) throws FormattedRuntimeException;

}
