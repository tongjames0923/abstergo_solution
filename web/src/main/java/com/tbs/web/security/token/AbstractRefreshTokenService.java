package com.tbs.web.security.token;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.exception.NotFoundTokenException;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.issuance.form.TokenRequestForm;
import com.tbs.web.security.user.IUserInfoService;
import com.tbs.web.security.user.access.IRefreshAccessTokenProvider;
import com.tbs.web.security.user.form.IRefreshTokenForm;
import com.tbs.web.uitl.AuthUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Optional;

/**
 * 抽象的刷新令牌服务
 * <p>
 * 模板实现 {@link IRefreshTokenService}：定义刷新令牌的创建、禁用与使用（换发访问令牌）流程。
 * 具体存储策略（如令牌存放于请求属性、Redis 等）由子类实现 {@code getRefreshToken}/{@code pushRefreshToken} 等抽象方法。
 * 创建与使用环节均会触发 {@link IRefreshTokenValidator} 校验链。
 * </p>
 *
 * @author abstergo
 * @see IRefreshTokenService 刷新令牌契约
 */
public abstract class AbstractRefreshTokenService implements IRefreshTokenService {

    @Resource
    private IRefreshAccessTokenProvider refreshAccessTokenProvider;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 从当前上下文获取刷新令牌串
     *
     * @return 刷新令牌串；当前无刷新令牌时返回空 Optional
     */
    protected abstract Optional<String> getRefreshToken();

    /**
     * 持久化刷新令牌
     *
     * @param refreshToken 已签发的刷新令牌模型，不能为空
     */
    protected abstract void pushRefreshToken( ITokenModel refreshToken);

    /**
     * 由刷新令牌模型构造换发访问令牌用的表单
     *
     * @param refreshToken 刷新令牌模型，不能为空
     * @return 访问令牌请求表单
     */
    protected abstract IRefreshTokenForm createRefreshTokenForm( ITokenModel refreshToken);

    /**
     * 获取刷新令牌验证器集合
     *
     * @return 验证器集合，无配置时返回空集合
     */
    protected abstract Collection<IRefreshTokenValidator> getRefreshTokenValidators();

    /**
     * 执行刷新令牌创建前的校验链
     *
     * @param userDetails  当前用户详情，不能为空
     * @param accessToken  关联的访问令牌模型，不能为空
     */
    private void createValidate(UserDetails userDetails, ITokenModel accessToken) {
        var validators = getRefreshTokenValidators();
        if (CollUtil.isEmpty(validators)) {
            return;
        }
        for (var v : validators) {
            v.validateCreate(userDetails, accessToken);
        }
    }

    @Resource
    private ITokenIssuanceCenter center;

    @Override
    public ITokenModel createRefreshToken() {
        ITokenModel currentTokenModel = AuthUtil.getInstance().getCurrentTokenModel();
        UserDetails userDetails = AuthUtil.getInstance().getCurrentUser();
        createValidate(userDetails, currentTokenModel);
        // 基于当前会话与用户签发刷新令牌，并落库/缓存
        ITokenModel refreshToken = center.issueToken(
            new TokenRequestForm(currentTokenModel.getSessionId(), userDetails, TokenTypeEnum.REFRESH_TOKEN.getCode(),
                TokenCreateTypeEnum.REFRESH.getCode()));
        pushRefreshToken(refreshToken);
        return refreshToken;
    }

    @Resource
    ITokenRemover remover;

    @Override
    public void inactiveRefreshToken() {
        String refreshToken = getRefreshToken().orElse(null);
        AssertUtil.hasText(refreshToken, "不存在刷新令牌");
        ITokenModel refreshTokenModel = center.getTokenByToken(refreshToken);
        AssertUtil.notNull(refreshTokenModel, "刷新令牌不存在");
        remover.inactiveToken(refreshTokenModel);
    }

    @Override
    public ITokenModel useRefreshToken() {
        String refreshToken = getRefreshToken().orElse(null);
        AssertUtil.isTrue(StrUtil.isEmpty(refreshToken), () -> new NotFoundTokenException("未读取到有效的刷新令牌"));
        ITokenModel refreshTokenModel = center.getTokenByToken(refreshToken);
        AssertUtil.isTrue(refreshTokenModel == null, () -> new InvalidTokenException("刷新令牌不存在"));
        UserDetails userDetails = userInfoService.loadUserInfoByUniqueKey(refreshTokenModel.getUserName());
        AssertUtil.isTrue(userDetails == null, () -> new InvalidTokenException("刷新令牌对应的用户详情数据不存在"));
        IRefreshTokenForm form = createRefreshTokenForm(refreshTokenModel);
        // 换发前再校验一次，确保用户状态未变更
        getRefreshTokenValidators().forEach(v -> v.validateUse(form, userDetails));
        ITokenModel model = refreshAccessTokenProvider.requestToken(form);
        // 用完即废：刷新令牌一次性使用，避免重放
        remover.inactiveToken(refreshTokenModel);
        return model;
    }
}
