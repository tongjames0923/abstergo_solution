package com.tbs.app.service.impl.web;

import com.tbs.web.uitl.AuthUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * 预授权工具类
 *
 * @author abstergo
 */
@Component
public class PreAuthUtils {

    public static final boolean isAccountActived(UserDetails d) {
        return d != null && d.isEnabled() && d.isAccountNonLocked();
    }


    public boolean isAccountActived() {
        var vo = AuthUtil.getInstance().getCurrentUser();
//        AssertUtil.isTrue().errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n("user.not.logined").throwIt();
//
//        AssertUtil.isTrue(!).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).i18n("auth.user.not.enabled")
//            .params(vo.getUsername()).throwIt();
//
//        AssertUtil.isTrue().errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
//            .i18n("auth.user.not.activated").params(vo.getUsername()).throwIt();

        return isAccountActived(vo);
    }

}
