package com.tbs.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 推送消息列表行视图
 * <p>
 * 消息中心分页列表的行数据：消息本体字段 + 当前用户的读状态派生值。 读状态由操作日志派生（readFlag/readTime），不落库存储。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushNotifyMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 业务主题
     */
    private String topic;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * 标题
     */
    private String title;

    /**
     * 正文
     */
    private String content;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 发送者展示标识
     */
    private String sender;

    /**
     * 点击跳转地址
     */
    private String url;

    /**
     * 发送时间（消息创建时间）
     */
    private Date createTime;

    /**
     * 是否已读（由 READ 操作日志派生）
     */
    private Boolean readFlag;

    /**
     * 首次已读时间（未读时为null）
     */
    private Date readTime;

}
