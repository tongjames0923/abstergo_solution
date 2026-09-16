package com.tbs.app.security.grant;

import com.tbs.app.service.identity.ExternalUserProfile;
import com.tbs.app.service.identity.IUserExternalIdentityService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.web.constant.WebI18nConstant;
import com.tbs.web.security.user.IUserInfoService;
import com.tbs.web.security.user.access.AbstractAccessTokenProvider;
import com.tbs.web.security.user.access.grant.ILoginGrantProvider;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * 外部身份登录授权提供者模板
 * <p>
 * 在 {@link AbstractAccessTokenProvider} 令牌签发模板之上，收敛外部身份登录的
 * 「平台凭证换取 → 绑定查询/自动关联 → 加载系统用户」流程；具体平台只需实现
 * {@link #getIdentityType()} 与 {@link #pullExternalIdentity(FORM)} 两个钩子，
 * 即免费获得绑定管理与自动关联能力（绑定规则单点收敛于
 * {@link IUserExternalIdentityService}）。
 * </p>
 *
 * @param <FORM> 凭证表单类型，需可序列化
 * @author abstergo
 * @see ILoginGrantProvider 登录授权提供者 SPI
 * @see IUserExternalIdentityService 外部身份绑定管理服务
 */
public abstract class AbstractExternalLoginGrantProvider<FORM extends Serializable>
    extends AbstractAccessTokenProvider<FORM> implements ILoginGrantProvider<FORM> {

    @Resource
    private IUserExternalIdentityService externalIdentityService;

    @Resource
    private ISystemUserService systemUserService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 获取外部身份类型
     * <p>
     * 与绑定表 identity_type 及 {@code IExternalIdentityExchanger#getIdentityType()} 对齐。
     * </p>
     *
     * @return 外部身份类型（如 wecom）
     */
    protected abstract String getIdentityType();

    /**
     * 平台凭证换取外部用户信息钩子
     * <p>
     * 建议委托对应平台的 {@code IExternalIdentityExchanger} 实现，与自助绑定链路共用换取逻辑。
     * </p>
     *
     * @param form 平台凭证表单
     * @return 外部用户信息载体（至少包含 externalId）
     */
    protected abstract ExternalUserProfile pullExternalIdentity(FORM form);

    @Override
    protected UserDetails pullUserDetails(FORM form) {
        ExternalUserProfile profile = pullExternalIdentity(form);
        AssertUtil.isTrue(profile == null || StrUtil.isEmpty(profile.getExternalId()))
            .i18n(WebI18nConstant.LOGIN_GRANT_PAYLOAD_INVALID).params(getIdentityType())
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        Long userId = externalIdentityService.autoBind(profile);
        AssertUtil.isTrue(userId == null).i18n(WebI18nConstant.LOGIN_GRANT_EXTERNAL_NOT_BOUND)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        SystemUserEntity user = systemUserService.queryById(userId);
        AssertUtil.isTrue(user == null).i18n(WebI18nConstant.LOGIN_GRANT_EXTERNAL_NOT_BOUND)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        return userInfoService.loadUserInfoByUniqueKey(user.getUserName());
    }
}
