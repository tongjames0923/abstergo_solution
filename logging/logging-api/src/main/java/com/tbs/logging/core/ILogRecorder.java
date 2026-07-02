package com.tbs.logging.core;

import com.tbs.logging.form.LoggingRecordForm;

/**
 * 日志记录器
 *
 * @author tongj
 */
public interface ILogRecorder {
    /**
     * 记录日志
     *
     * @param recordForm 日志记录表单
     */
    void record(LoggingRecordForm recordForm);

    /**
     * 刷写日志
     */
    void flush();
}
