package com.tbs.app.service.impl.push;

import com.tbs.app.constant.SystemI18nConstant;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.push.api.auth.IPushAuthenticator;
import com.tbs.push.constants.PushSessionAttrConstants;
import com.tbs.push.model.auth.PushAuthForm;
import com.tbs.push.model.auth.PushAuthResult;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务连接认证器（签票时点身份绑定落地点）
 * <p>
 * 覆盖协议层 {@code NoopAuthenticator} 兜底：不采信表单声明的 token/userId，
 * 身份以网关认证上下文为唯一权威——取当前登录用户名解析数字用户ID，
 * 服务端解析值<b>覆盖</b>表单值写入会话属性（覆盖而非拒绝：伪造者只会绑定到
 * 自己的真实身份，无利可图，同时不打断存量客户端）；声明与解析不一致仅记审计日志。
 * 认证失败返回失败结果（原因走 i18n 文本），由凭证服务统一抛协议异常。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class PushUserAuthenticator implements IPushAuthenticator {

    /**
     * 系统用户服务（登录用户名到数字用户ID的解析）
     */
    @Resource
    private ISystemUserService systemUserService;

    /**
     * 执行认证：以网关认证主体为准绑定会话身份
     *
     * @param form 客户端提交的认证表单（token/userId 声明值仅作参考不作信任源）
     * @return 认证结果（通过时携带服务端解析的 userId/username 属性）
     */
    @Override
    public PushAuthResult authenticate(PushAuthForm form) {
        String username;
        try {
            username = AuthUtil.getInstance().getCurrentUser().getUsername();
        } catch (Exception error) {
            log.warn("推送签票无法获取当前登录身份", error);
            return PushAuthResult.failure(I18nUtil.get(SystemI18nConstant.PUSH_AUTH_IDENTITY_MISSING));
        }
        List<Long> ids = systemUserService.queryUserIdsByUserName(username);
        if (CollUtil.isEmpty(ids)) {
            return PushAuthResult.failure(I18nUtil.get(SystemI18nConstant.PUSH_MESSAGE_USER_NOT_FOUND));
        }
        String resolvedUserId = String.valueOf(CollUtil.getFirst(ids));
        Map<String, String> attributes = new LinkedHashMap<>();
        if (form != null && form.getAttributes() != null) {
            attributes.putAll(form.getAttributes());
        }
        if (form != null && form.getUserId() != null && !form.getUserId().equals(resolvedUserId)) {
            log.warn("推送签票声明身份与登录主体不一致, declared:{}, resolved:{}, 以服务端解析为准",
                form.getUserId(), resolvedUserId);
        }
        attributes.put(PushSessionAttrConstants.USER_ID, resolvedUserId);
        attributes.put(PushSessionAttrConstants.USERNAME, username);
        return PushAuthResult.success(attributes);
    }

}
