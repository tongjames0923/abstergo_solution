package com.tbs.logging.core;

import com.tbs.common.error.AssertUtil;
import com.tbs.logging.form.LoggingRecordForm;

/**
 * 默认日志上下文回填实现
 * <p>
 * 将上下文标准字段（链路追踪 ID、操作用户、IP、请求 URL、请求方法） 回填到日志表单对应字段；扩展属性不参与回填，由有需要的采集入口自行读取。
 * </p>
 *
 * @author abstergo
 * @see ILoggingContextApplier
 */
public class DefaultLoggingContextApplier implements ILoggingContextApplier {

    @Override
    public void apply(ILoggingContext context, LoggingRecordForm form) {
        AssertUtil.notNull(context, "日志上下文不能为空");
        AssertUtil.notNull(form, "日志记录表单不能为空");
        context.getTraceId().ifPresent(form::setTraceId);
        context.getOperateUser().ifPresent(form::setOperateUser);
        context.getOperateIp().ifPresent(form::setOperateIp);
        context.getRequestUrl().ifPresent(form::setOperateUrl);
        context.getRequestMethod().ifPresent(form::setMethod);
    }
}
