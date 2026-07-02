package com.tbs.logging.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 日志记录表单
 *
 * @author tongj
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggingRecordForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 操作类型
     */
    private String operateType;
    /**
     * 操作描述
     */
    private String operateDesc;
    /**
     * 操作用户
     */
    private String operateUser;
    /**
     * 操作IP
     */
    private String operateIp;
    /**
     * 操作时间
     */
    private Date operateTime;
    /**
     * 操作结果
     */
    private String operateResult;
    /**
     * 操作参数
     */
    private String operateParam;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误码
     */
    private Integer errorCode;

    /**
     * 操作完成时间
     */
    private Date operateFinishTime;

    /**
     * 请求URL
     */
    private String operateUrl;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 刷新标志
     */
    private Integer flushFlag;
}
