package com.tbs.app.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 动作测试执行回执。
 *
 * @author abstergo
 */
@Data
@Builder
public class EventActionTestResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 动作ID
     */
    private String actionId;

    /**
     * 是否执行成功
     */
    private boolean success;

    /**
     * 执行耗时，单位毫秒
     */
    private long elapsedMs;

    /**
     * 失败原因，成功时为空
     */
    private String errorMsg;
}
