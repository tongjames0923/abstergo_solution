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
 *
 * @author abstergo
 */
public abstract class AbstractRefreshTokenService implements IRefreshTokenService {

    @Resource
    private IRefreshAccessTokenProvider refreshAccessTokenProvider;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 获取refreshToken
     *
     * @return
     */
    protected abstract Optional<String> getRefreshToken();

    /**
     * 存储refreshToken
     *
     * @param refreshToken
     */
    protected abstract void pushRefreshToken( ITokenModel refreshToken);

    /**
     * 创建刷新令牌表单
     *
     * @param refreshToken
     * @return
     */
    protected abstract IRefreshTokenForm createRefreshTokenForm( ITokenModel refreshToken);

    /**
     * 获取刷新令牌验证器
     *
     * @return
     */
    protected abstract Collection<IRefreshTokenValidator> getRefreshTokenValidators();

    /**
     * 验证refreshToken
     *
     * @param userDetails
     * @param accessToken
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
        getRefreshTokenValidators().forEach(v -> v.validateUse(form, userDetails));
        ITokenModel model = refreshAccessTokenProvider.requestToken(form);
        remover.inactiveToken(refreshTokenModel);
        return model;
    }
}
