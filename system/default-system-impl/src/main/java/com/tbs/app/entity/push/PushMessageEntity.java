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
 * 推送业务消息主表实体
 * <p>
 * 消息本体唯一存储（不随接收人复制），发送后不可变；读状态不在本表 （由操作日志表派生）。日志类数据，保留期外物理清理。 无任何数据库业务约束（无唯一键/外键），唯一性由服务端写入路径保证。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Entity
@Table(name = "push_message", indexes = {@Index(name = "idx_create_time", columnList = "create_time")})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PushMessageEntity
    implements IIdentifiable<Long>, IUpdateTimeAuditable, ICreateTimeAuditable, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键（即业务消息标识）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 业务主题
     */
    @Column(name = "topic", nullable = false, length = 128)
    private String topic;

    /**
     * 消息类型
     */
    @Column(name = "msg_type", nullable = false, length = 32)
    private String msgType;

    /**
     * 标题
     */
    @Column(name = "title", length = 255)
    private String title;

    /**
     * 正文
     */
    @Column(name = "content")
    private String content;

    /**
     * 优先级
     */
    @Column(name = "priority", nullable = false, length = 16)
    private String priority;

    /**
     * 发送者展示标识（审计权威见操作日志表 SEND 记录）
     */
    @Column(name = "sender", length = 64)
    private String sender;

    /**
     * 点击跳转地址
     */
    @Column(name = "url", length = 512)
    private String url;

    /**
     * 扩展信息（JSON，业务自定义字段）
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
