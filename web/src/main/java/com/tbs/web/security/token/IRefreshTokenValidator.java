package com.tbs.web.security.token;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.web.security.user.form.IRefreshTokenForm;
import org.springframework.core.Ordered;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 刷新令牌创建验证器
 * <p>
 * 在刷新令牌创建与使用两个节点做附加校验（如访问令牌状态、用户绑定一致性等）。
 * 通过 {@link Ordered} 支持多个验证器按序串联。
 * </p>
 *
 * @author abstergo
 * @see Ordered 决定验证器执行顺序
 */
public interface IRefreshTokenValidator extends Ordered {
    /**
     * 验证刷新令牌的创建
     *
     * @param userDetails 当前用户详情，不能为空
     * @param accessToken 关联的访问令牌模型，不能为空
     * @throws FormattedRuntimeException 当校验不通过时抛出，携带业务可读错误信息
     */
    void validateCreate(UserDetails userDetails, ITokenModel accessToken) throws FormattedRuntimeException;

    /**
     * 验证刷新令牌的使用
     *
     * @param form        刷新令牌使用表单，不能为空
     * @param userDetails 当前用户详情，不能为空
     * @throws FormattedRuntimeException 当校验不通过时抛出，携带业务可读错误信息
     */
    void validateUse(IRefreshTokenForm form, UserDetails userDetails);

}
