package com.tbs.logging.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 日志记录提取表单
 *
 * @author tongj
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggingRecordExtractForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 操作类型
     */
    private List<String> operateTypes;

    /**
     * 操作时间
     */
    private Date operateTimeBegin;

    /**
     * 操作时间
     */
    private Date operateTimeEnd;

}
