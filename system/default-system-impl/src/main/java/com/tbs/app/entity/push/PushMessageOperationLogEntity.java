package com.tbs.app.entity.push;

import com.tbs.shared.pojo.ICreateTimeAuditable;
import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.shared.pojo.IUpdateTimeAuditable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
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
public class PushMessageOperationLogEntity
    implements IIdentifiable<Long>, IUpdateTimeAuditable, ICreateTimeAuditable, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 消息ID（push_message.id）
     */
    @Column(name = "message_id", nullable = false)
    private Long messageId;

    /**
     * 操作人（系统/服务发起时可空，身份进 ext_info）
     */
    @Column(name = "operator")
    private Long operator;

    /**
     * 操作类型（SEND/READ/CLICK/...，取值见 PushMessageOperationTypeEnum）
     */
    @Column(name = "op_type", nullable = false, length = 32)
    private String opType;

    /**
     * 来源通道（http/rpc/job/system）
     */
    @Column(name = "op_source", length = 32)
    private String opSource;

    /**
     * 操作时间
     */
    @Column(name = "op_time", nullable = false)
    private Date opTime;

    /**
     * 操作上下文扩展（JSON，服务名、调用方标识、点击位置等）
     */
    @Column(name = "ext_info")
    private String extInfo;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

}
