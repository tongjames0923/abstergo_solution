package com.tbs.shared.pojo.entity.push;

import com.tbs.shared.pojo.ICreateTimeAuditable;
import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.shared.pojo.IUpdateTimeAuditable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 推送消息接收人表实体
 * <p>
 * fan-out 投递关系事实（一条消息对每个接收人一行），append-only 纯插入； 读状态不在本表（由操作日志表派生）。自增主键即到达序，兼作游标分页排序键。 关系类数据，保留期外物理清理。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Entity
@Table(name = "push_message_receiver", indexes = {@Index(name = "idx_receiver", columnList = "receiver"),
    @Index(name = "idx_create_time", columnList = "create_time")})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PushMessageReceiverEntity
    implements IIdentifiable<Long>, IUpdateTimeAuditable, ICreateTimeAuditable, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键（兼游标分页排序键，雪花值按时间递增，即到达序）
     */
    @Column(name = "id")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 消息ID（push_message.id）
     */
    @Column(name = "message_id", nullable = false)
    private Long messageId;

    /**
     * 接收用户ID
     */
    @Column(name = "receiver", nullable = false)
    private Long receiver;

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
