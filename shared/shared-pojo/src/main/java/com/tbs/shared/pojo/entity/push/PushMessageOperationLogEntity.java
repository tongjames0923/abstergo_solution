package com.tbs.shared.pojo.entity.push;

import com.tbs.shared.pojo.ICreateTimeAuditable;
import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.shared.pojo.IUpdateTimeAuditable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 消息全链路操作日志实体
 * <p>
 * 消息生命周期行为事件流水（SEND/READ/CLICK/...），append-only 纯插入， 重复记录不破坏语义（读状态派生用 DISTINCT 消化）： 已读 ⇔ 存在 (operator, message_id,
 * op_type=READ) 记录。 SEND 记录覆盖 RPC/Job 等无请求日志通道的发送审计（op_source 声明来源通道）。 操作行为日志，保留期可独立于关系表设置。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Entity
@Table(name = "push_message_operation_log",
    indexes = {@Index(name = "idx_operator_msg", columnList = "operator,message_id"),
        @Index(name = "idx_message", columnList = "message_id"), @Index(name = "idx_op_time", columnList = "op_time")})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Comment("推送消息全链路操作日志表")
public class PushMessageOperationLogEntity
    implements IIdentifiable<Long>, IUpdateTimeAuditable, ICreateTimeAuditable, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Comment("主键（雪花算法生成）")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 消息ID（push_message.id）
     */
    @Comment("消息ID（关联 push_message.id）")
    @Column(name = "message_id", nullable = false)
    private Long messageId;

    /**
     * 操作人（系统/服务发起时可空，身份进 ext_info）
     */
    @Comment("操作人用户ID（系统/服务发起时可空，身份进 ext_info）")
    @Column(name = "operator")
    private Long operator;

    /**
     * 操作类型（SEND/READ/CLICK/...，取值见 PushMessageOperationTypeEnum）
     */
    @Comment("操作类型：SEND-发送，READ-已读，CLICK-点击")
    @Column(name = "op_type", nullable = false, length = 32)
    private String opType;

    /**
     * 来源通道（http/rpc/job/system）
     */
    @Comment("操作来源通道（http/rpc/job/system）")
    @Column(name = "op_source", length = 32)
    private String opSource;

    /**
     * 操作时间
     */
    @Comment("操作发生时间")
    @Column(name = "op_time", nullable = false)
    private Date opTime;

    /**
     * 操作上下文扩展（JSON，服务名、调用方标识、点击位置等）
     */
    @Comment("操作上下文扩展（JSON，服务名、调用方标识、点击位置等）")
    @Column(name = "ext_info")
    private String extInfo;

    /**
     * 创建时间
     */
    @Comment("创建时间")
    @Column(name = "create_time", nullable = false)
    private Date createTime;

    /**
     * 更新时间
     */
    @Comment("更新时间")
    @Column(name = "update_time")
    private Date updateTime;

}
