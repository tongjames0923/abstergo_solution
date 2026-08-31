package com.tbs.web.gate;

import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.exception.NotFoundTokenException;
import com.tbs.web.security.token.ITokenModel;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * 获取用户登录 Token 的接口
 * <p>
 * 从请求中按既定策略（header/cookie/param/body）提取令牌串，供网关中心构造认证上下文。
 * </p>
 *
 * @author tongj
 * @see NotFoundTokenException 令牌缺失
 * @see InvalidTokenException 令牌非法
 */
public interface ITokenPicker {
    /**
     * 从请求中提取登录令牌
     *
     * @param request HTTP 请求，不能为空
     * @return 提取到的令牌串
     * @throws NotFoundTokenException 当请求中找不到令牌时抛出
     * @throws InvalidTokenException  当令牌格式/内容非法时抛出
     */
    String pick( HttpServletRequest request) throws NotFoundTokenException;
}
