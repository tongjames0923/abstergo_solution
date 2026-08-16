package com.tbs.logging.core;

import com.tbs.logging.form.LoggingRecordExtractForm;
import com.tbs.logging.vo.LogRecordVO;

import java.util.List;

/**
 * 日志提取器接口
 *
 * @author tongj
 */
public interface ILogExtractor {
    /**
     * 提取日志
     *
     * @param form 日志提取参数
     * @return 日志列表
     */
    List<LogRecordVO> extract(LoggingRecordExtractForm form);

    /**
     * 统计符合条件的日志总数
     *
     * @param form 日志提取参数
     * @return 总数
     */
    int count(LoggingRecordExtractForm form);

    /**
     * 获取所有日志类型
     *
     * @return 日志类型列表
     */
    List<String> getAllOperateTypes();
}
