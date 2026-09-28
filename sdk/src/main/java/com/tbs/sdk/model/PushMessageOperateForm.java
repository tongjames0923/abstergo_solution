package com.tbs.sdk.model;

import lombok.Data;

/**
 * 消息操作回传表单
 * <p>
 * 对应服务端PushMessageOperateForm：messageId与opType必填，
 * opType为除READ外的处理行为（如CLICK，服务端对CLICK默认补记READ）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushMessageOperateForm {

    /**
     * 消息ID（必填）
     */
    private Long messageId;

    /**
     * 操作类型（必填，如CLICK）
     */
    private String opType;

    /**
     * 扩展信息（可选）
     */
    private String extInfo;
}
