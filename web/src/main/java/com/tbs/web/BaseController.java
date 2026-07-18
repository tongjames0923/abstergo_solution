package com.tbs.web;

import com.tbs.common.i18n.I18nUtil;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import lombok.Getter;

/**
 * 基础的控制器, 提供获取当前用户和TokenModel的方法
 *
 * @author tongj
 */
@Getter
public abstract class BaseController {

    /**
     * 获取当前控制器的业务名称
     *
     * @return
     */
    public abstract String getBusinessName();

    /**
     * 默认执行的操作成功返回值
     *
     * @param val
     * @return
     */
    public AjaxResult successResult(Object val) {
        return AjaxUtil.getInstance().success(I18nUtil.get("common.success"), val);
    }

    /**
     * 默认执行的操作失败返回值
     *
     * @param e
     * @return
     */
    public AjaxResult errorResult(Throwable e) {
        return AjaxUtil.getInstance().error(AjaxResult.getDefaultErrorMessage() + "。" + e.getMessage(), e);
    }

}