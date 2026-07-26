package com.tbs.web.result.impl;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.web.exception.ReLoginRequestException;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IOnErrorAjaxProvider;
import com.tbs.web.uitl.AjaxUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 重新登录错误处理ajax结果提供者
 *
 * @author tongj
 */
public class ReLoginErrorAjaxProvider implements IOnErrorAjaxProvider<ReLoginRequestException> {
    @Override
    public AjaxResult onError(ReLoginRequestException throwable) {
        return AjaxUtil.getInstance().custom(AjaxResult.RELOGIN_CODE,
            StrUtil.emptyToDefault(throwable.getLocalizedMessage(), AjaxResult.getDefaultReLoginMessage()), null);
    }

    @Override
    public boolean support( Class<? extends Throwable> clazz) {
        return BeanUtil.isBaseFrom(clazz, ReLoginRequestException.class);
    }

}
