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
     * @return 业务名称，用于日志/审计标识
     */
    public abstract String getBusinessName();

    /**
     * 默认操作成功响应（带业务数据）
     *
     * @param val 业务数据载荷，可为 {@code null}
     * @return 统一成功响应
     */
    public AjaxResult successResult(Object val) {
        return AjaxUtil.getInstance().success(I18nUtil.get("common.success"), val);
    }

    /**
     * 默认操作成功响应（无数据）
     *
     * @return 统一成功响应
     */
    public AjaxResult successResult() {
        return AjaxUtil.getInstance().success(I18nUtil.get("common.success"), null);
    }

    /**
     * 默认操作失败响应
     *
     * @param e 异常，其消息会拼接到默认错误消息后以辅助排查
     * @return 统一错误响应
     */
    public AjaxResult errorResult(Throwable e) {
        return AjaxUtil.getInstance().error(AjaxResult.getDefaultErrorMessage() + "。" + e.getMessage(), e);
    }

}
