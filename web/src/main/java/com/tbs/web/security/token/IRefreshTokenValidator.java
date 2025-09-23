package com.tbs.web.security.token;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.web.security.user.form.IRefreshTokenForm;
import org.springframework.core.Ordered;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 刷新令牌创建验证器
 *
 * @author abstergo
 */
public interface IRefreshTokenValidator extends Ordered {
    /**
     * 验证刷新令牌创建
     *
     * @param form
     * @param userDetails
     * @param accessToken
     * @throws FormattedRuntimeException
     */
    void validateCreate(UserDetails userDetails, ITokenModel accessToken) throws FormattedRuntimeException;

    /**
     * 验证刷新令牌使用
     *
     * @param form
     * @param userDetails
     * @param accessToken
     * @throws FormattedRuntimeException
     */
    void validateUse(IRefreshTokenForm form, UserDetails userDetails);

}
