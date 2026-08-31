package com.tbs.web.result.impl;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.web.exception.ReLoginRequestException;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IOnErrorAjaxProvider;
import com.tbs.web.uitl.AjaxUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 重新登录错误处理 ajax 结果提供者
 * <p>
 * 专门处理 {@link ReLoginRequestException}：返回「需重新登录」语义码（{@link AjaxResult#RELOGIN_CODE}），
 * 提示前端跳转登录页。仅对 {@code ReLoginRequestException} 及其子类生效。
 * </p>
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
