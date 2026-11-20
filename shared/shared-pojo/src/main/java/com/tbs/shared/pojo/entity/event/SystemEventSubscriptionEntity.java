package com.tbs.shared.pojo.entity.event;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 事件订阅实体。
 *
 * <p>把「某个事件的某个触发器」固定下来：{@code (reporterId, eventCode)} 定位事件，
 * {@code triggerId} 指定触发器，{@code ruleCode} 绑定条件规则编码（可空），
 * {@code sortOrder} 决定同一事件下多个触发器的执行顺序。{@code enabled} 控制是否参与分发，
 * 与逻辑删除标志 {@code flag} 相互独立。</p>
 *
 * @author abstergo
 */
@Entity
@Table(name = "sys_event_subscription", indexes = {
    @Index(name = "idx_event_subscription_event", columnList = "reporter_id,event_code,sort_order"),
    @Index(name = "idx_event_subscription_enabled", columnList = "enabled"),
    @Index(name = "idx_event_subscription_flag", columnList = "flag")
})
@LogicDeleteEntity(activeFlagValues = {1})
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SystemEventSubscriptionEntity
    implements IBaseEntity<Long>, ICommonCreateAuditable<Long>, ICommonUpdateAuditable<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Column(name = "id")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 事件上报器ID
     */
    @Column(name = "reporter_id", nullable = false, length = 128)
    private String reporterId;

    /**
     * 事件编码，仅在所属上报器内唯一
     */
    @Column(name = "event_code", nullable = false, length = 128)
    private String eventCode;

    /**
     * 触发器ID
     */
    @Column(name = "trigger_id", nullable = false, length = 128)
    private String triggerId;

    /**
     * 条件规则编码，为空表示该绑定不做条件判定
     */
    @Column(name = "rule_code", length = 64)
    private String ruleCode;

    /**
     * 执行顺序，升序；为空时排在同事件其余订阅之后
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 是否启用，1 启用 / 0 停用
     */
    @Column(name = "enabled", nullable = false)
    private Integer enabled;

    /**
     * 逻辑删除标志
     *
     * @see com.tbs.shared.enums.CommonFlagEnum
     */
    @Column(name = "flag")
    private Integer flag;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 创建者
     */
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 更新者
     */
    @Column(name = "update_by")
    private Long updateBy;
}
