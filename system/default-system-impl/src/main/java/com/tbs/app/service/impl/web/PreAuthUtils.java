package com.tbs.app.service.impl.web;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.user.model.impl.DefaultGrantedAuthority;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.shared.enums.SystemRoleTypeEnum;
import com.tbs.web.uitl.AuthUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Objects;
import com.tbs.app.constant.SystemI18nConstant;

/**
 * 预授权工具类
 *
 * @author abstergo
 */
@Component
public class PreAuthUtils {

    @Resource
    private AppSettingProperty appSettingProperty;

    public static final boolean isAccountActived(UserDetails d) {
        return d != null && d.isEnabled() && d.isAccountNonLocked();
    }

    /**
     * 验证当前用户是否具有指定的API权限
     *
     * @param permission
     * @return
     */
    public static final boolean hasPermission(String permission) {
        return SpringUtil.getBean(PreAuthUtils.class).hasApiPermission(permission);
    }

    public boolean hasApiPermission(String permission) {
        if (!appSettingProperty.isEnableApiPermission()) {
            return true;
        }
        if (StrUtil.isEmpty(permission)) {
            return true;
        }
        var vo = AuthUtil.getInstance().getCurrentUser();
        if (!isAccountActived(vo)) {
            return false;
        }
        var authorities = vo.getAuthorities();
        if (CollUtil.isEmpty(authorities)) {
            return false;
        }
        for (var authority : authorities) {
            if (authority == null) {
                continue;
            }
            if (authority instanceof DefaultGrantedAuthority) {
                var grantedAuthority = (DefaultGrantedAuthority)authority;
                var roleGroup = grantedAuthority.getRoleGroup();
                if (roleGroup == null) {
                    continue;
                }
                var roles = roleGroup.getPermissions();
                if (CollUtil.isEmpty(roles)) {
                    continue;
                }
                for (var role : roles) {
                    if (!Objects.equals(SystemRoleTypeEnum.API_ROLE.getCode(), role.getType())) {
                        continue;
                    }
                    if (Objects.equals(role.getRole(), permission)) {
                        return true;
                    }
                }

            } else {
                return Objects.equals(authority.getAuthority(), permission);
            }
        }
        return false;

    }

    /**
     * 验证当前用户是否激活
     */
    public boolean isAccountActived() {
        var vo = AuthUtil.getInstance().getCurrentUser();
//        AssertUtil.isTrue().errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n(com.tbs.app.constant.SystemI18nConstant.USER_NOT_LOGINED).throwIt();
//
//        AssertUtil.isTrue(!).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).i18n(com.tbs.app.constant.SystemI18nConstant.AUTH_USER_NOT_ENABLED)
//            .params(vo.getUsername()).throwIt();
//
//        AssertUtil.isTrue().errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
//            .i18n(com.tbs.app.constant.SystemI18nConstant.AUTH_USER_NOT_ACTIVATED).params(vo.getUsername()).throwIt();

        return isAccountActived(vo);
    }

}
