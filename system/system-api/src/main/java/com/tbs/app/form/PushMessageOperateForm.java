package com.tbs.app.form;

import lombok.Data;

import java.io.Serializable;

/**
 * 推送消息操作回传表单
 * <p>
 * 用户对消息的处理行为回传（CLICK 等扩展操作）：服务端校验接收关系后写入操作日志。 READ 操作请使用 IPushNotifyService.markRead 批量通道。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushMessageOperateForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID（必填）
     */
    private Long messageId;

    /**
     * 操作类型（必填，取值见 PushMessageOperationTypeEnum，除 READ 外）
     */
    private String opType;

    /**
     * 操作上下文扩展（可选，如点击位置、来源端）
     */
    private String extInfo;

}
