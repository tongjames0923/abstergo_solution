package com.tbs.logging.core;

import com.tbs.logging.form.LoggingRecordForm;

/**
 * 日志上下文回填器——将 {@link ILoggingContext} 快照映射到日志表单
 * <p>
 * 独立于上下文契约存在：日志表单字段调整时仅变更回填器实现，
 * 不影响上下文契约及其创建、增强链路。
 * </p>
 *
 * @author abstergo
 * @see ILoggingContext
 * @see ILogRecorder
 */
public interface ILoggingContextApplier {

    /**
     * 将上下文快照回填到日志表单
     * <p>
     * 仅覆盖上下文可提供的环境维度字段（链路追踪 ID、操作用户、IP 等），
     * 采集入口自行填充的字段（操作类型、描述、时间、参数等）不受影响。
     * </p>
     *
     * @param context 日志上下文快照
     * @param form    日志记录表单
     */
    void apply(ILoggingContext context, LoggingRecordForm form);
}
