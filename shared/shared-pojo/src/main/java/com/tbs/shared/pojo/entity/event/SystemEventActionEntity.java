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
 * 事件动作绑定实体。
 *
 * <p>把「某个事件命中条件后要执行哪些动作」固定下来：{@code (reporterId, eventCode)} 定位事件，
 * {@code actionId} 指定动作实现，{@code paramJson} 保存 {@code List<ParamValue>} 形式的动作参数，
 * {@code sortOrder} 决定执行顺序。{@code actionName} 仅作展示快照，运行期以 {@code actionId} 为准。</p>
 *
 * @author abstergo
 */
@Entity
@Table(name = "sys_event_action", indexes = {
    @Index(name = "idx_event_action_event", columnList = "reporter_id,event_code,sort_order"),
    @Index(name = "idx_event_action_enabled", columnList = "enabled"),
    @Index(name = "idx_event_action_flag", columnList = "flag")
})
@LogicDeleteEntity(activeFlagValues = {1})
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SystemEventActionEntity
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
     * 动作ID
     */
    @Column(name = "action_id", nullable = false, length = 128)
    private String actionId;

    /**
     * 动作名称展示快照，可为空
     */
    @Column(name = "action_name", length = 128)
    private String actionName;

    /**
     * 运行时动作参数，ParamValue 列表的 JSON 表示
     */
    @Column(name = "param_json", columnDefinition = "TEXT")
    private String paramJson;

    /**
     * 执行顺序，升序；为空时排在同事件其余动作之后
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
