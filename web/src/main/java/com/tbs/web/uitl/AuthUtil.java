package com.tbs.web.uitl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.web.exception.NoLoginException;
import com.tbs.web.security.authentication.IAuthManager;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.security.token.ITokenModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * 认证工具类
 * <p>
 * 提供了获取当前用户认证信息、检查用户登录状态、验证用户详情等安全相关功能的工具方法。 该类采用单例模式设计，可通过getInstance()方法获取实例。
 * </p>
 *
 * @author tongj
 */
@Component
@Slf4j
public class AuthUtil {

    /**
     * 静态实例引用，用于实现单例模式
     */
    private static AuthUtil instance;

    @Resource
    IAuthManager authManager;

    /**
     * 获取认证工具类的单例实例
     *
     * @return 认证工具类的单例实例
     */
    public static AuthUtil getInstance() {
        return instance;
    }

    /**
     * 构造函数，初始化单例实例
     */
    public AuthUtil() {
        instance = this;
    }

    /**
     * 获取当前用户的TokenModel
     * <p>
     * 从安全上下文中获取当前用户的认证信息，并提取其中的令牌模型。 如果用户未登录，则抛出NoLoginException异常。
     * </p>
     *
     * @return 当前用户的令牌模型
     * @throws NoLoginException          当用户未登录时抛出
     * @throws FormattedRuntimeException 当令牌获取失败时抛出
     */
    public ITokenModel getCurrentTokenModel() {
        return authManager.getCurrentTokenModel();
        //        AssertUtil.isTrue(!isUserLoggedIn(), () -> new NoLoginException("当前用户未登录"));
        //        return getAuthentication().map(ITokenAuthentication::getTokenModel)
        //            .orElseThrow(() -> new FormattedRuntimeException("用户Token获取失败"));
    }

    /**
     * 获取当前用户详细信息
     * <p>
     * 从安全上下文中获取当前用户的认证信息，并提取其中的用户详细信息。 如果用户未登录，则抛出NoLoginException异常。
     * </p>
     *
     * @return 当前用户的详细信息
     * @throws NoLoginException          当用户未登录时抛出
     * @throws FormattedRuntimeException 当用户数据获取失败时抛出
     */
    public UserDetails getCurrentUser() {
        AssertUtil.isTrue(!isUserLoggedIn(), () -> new NoLoginException("当前用户未登录"));
        return getAuthentication().map(ITokenAuthentication::getUserDetails)
            .orElseThrow(() -> new FormattedRuntimeException("用户数据获取失败"));
    }

    /**
     * 判断当前用户是否已认证（已登录）
     * <p>
     * 检查安全上下文中是否存在有效的认证信息，并且该认证信息包含有效的令牌且已通过认证。
     * </p>
     *
     * @return 如果用户已登录并认证成功返回true，否则返回false
     */
    public boolean isUserLoggedIn() {
        //        return getAuthentication().map(authentication -> authentication.hasToken() && authentication.isAuthenticated())
        //            .orElse(false);
        return authManager.isUserLoggedIn();
    }
    //
    //    @Resource
    //    private ITokenIssuanceCenter tokenIssuanceCenter;
    //
    //    @Resource
    //    private IUserLoginInfoService loginInfoService;
    //
    //    @Resource
    //    ITokenRemover remover;

    /**
     * 通知用户认证状态变更
     * <p>
     * 当用户认证状态发生变更时（如密码修改、权限变更等），调用此方法通知相关服务进行处理。
     * </p>
     *
     * @param userName 用户名
     */

    @Async
    public void notifyUserAuthStatusChanged(String userName) {

        authManager.notifyUserAuthStatusChanged(userName);
    }

    /**
     * 检查用户详情
     * <p>
     * 验证用户详细信息是否有效，包括用户是否存在、是否启用、是否过期、是否被锁定等状态检查。
     * </p>
     *
     * @param details 用户详细信息
     * @throws FormattedRuntimeException 当用户详情验证失败时抛出相应异常： - 用户详情不存在 - 用户已禁用 - 用户已过期 - 用户已锁定
     */
    public static void checkUserDetails(UserDetails details) throws FormattedRuntimeException {
        AssertUtil.isTrue(details == null).errorLevel(ErrorLevelEnum.SYSTEM_ERROR).message("用户详情不存在").throwIt();
        AssertUtil.hasText(details.getUsername(), "用户信息中用户名不能为空");
        AssertUtil.hasText(details.getPassword(), "用户信息中密码不能为空");
        AssertUtil.isTrue(!details.isEnabled()).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).message("用户已禁用")
            .throwIt();
        AssertUtil.isTrue(!details.isAccountNonExpired()).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .message("用户已过期").throwIt();
        AssertUtil.isTrue(!details.isAccountNonLocked()).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .message("用户已锁定").throwIt();
        AssertUtil.isTrue(!details.isCredentialsNonExpired()).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .message("用户凭证已过期").throwIt();

    }

    /**
     * 获取当前用户的认证信息
     * <p>
     * 从Spring Security的上下文中获取当前用户的认证信息，并将其转换为ITokenAuthentication类型。
     * </p>
     *
     * @return 包含当前用户认证信息的Optional对象，如果不存在或类型不匹配则返回空Optional
     */
    public Optional<ITokenAuthentication> getAuthentication() {
        return Optional.ofNullable(authManager.getCurrentAuthentication());
    }

    public Optional<ITokenAuthentication> getAuthentication(String token) {
        return Optional.ofNullable(authManager.getAuthentication(token));
    }

    /**
     * 设置当前用户的认证信息
     * <p>
     * 将指定的认证信息设置到Spring Security的上下文中，用于用户登录后的认证信息保存。
     * </p>
     *
     * @param authentication 用户认证信息
     */
    public void setAuthentication(ITokenAuthentication authentication) {
        authManager.setAuthentication(authentication);
    }

    public void clearAuthentication() {
        authManager.clearAuthentication();
    }

    public void clearAuthentication(String token) {
        authManager.clearAuthentication(token);
    }

}
