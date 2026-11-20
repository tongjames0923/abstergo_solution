package com.tbs.shared.pojo.entity.condition;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * 条件规则实体。
 *
 * <p>把值获取器编排出的判断逻辑固定下来：{@code getterId} 与 {@code invocationJson} 是运行时唯一可信输入，
 * {@code editorJson} 保存前端构建器状态用于再次编辑；{@code enabled} 控制规则是否参与求值，
 * 与逻辑删除标志 {@code flag} 相互独立。</p>
 *
 * @author abstergo
 */
@Entity
@Table(name = "sys_condition_rule", indexes = {
    @Index(name = "idx_condition_rule_code", columnList = "rule_code"),
    @Index(name = "idx_condition_rule_enabled", columnList = "enabled"),
    @Index(name = "idx_condition_rule_flag", columnList = "flag")
})
@LogicDeleteEntity(activeFlagValues = {1})
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SystemConditionRuleEntity
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
     * 规则编码，全局唯一
     */
    @Column(name = "rule_code", nullable = false, length = 64)
    private String ruleCode;

    /**
     * 规则名称
     */
    @Column(name = "rule_name", nullable = false, length = 128)
    private String ruleName;

    /**
     * 规则说明
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 顶层取值器 ID
     */
    @Column(name = "getter_id", nullable = false, length = 128)
    private String getterId;

    /**
     * 运行时调用载荷，ParamValue 列表的 JSON 表示
     */
    @Column(name = "invocation_json", nullable = false, columnDefinition = "TEXT")
    private String invocationJson;

    /**
     * 前端编辑器状态快照 JSON
     */
    @Column(name = "editor_json", columnDefinition = "TEXT")
    private String editorJson;

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
