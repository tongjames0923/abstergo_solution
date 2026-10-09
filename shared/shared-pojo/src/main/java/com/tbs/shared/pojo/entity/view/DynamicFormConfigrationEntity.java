package com.tbs.shared.pojo.entity.view;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.IIdentifiable;
import org.hibernate.annotations.Comment;
import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * 动态表单配置实体
 *
 * <p>用于定义动态表单的全局配置，包括布局、栅格等属性。</p>
 *
 * @author abstergo
 */
@Entity
@Table(name = "dynamic_form_configration",
    indexes = {@jakarta.persistence.Index(columnList = "flag"), @jakarta.persistence.Index(columnList = "title"),
        @jakarta.persistence.Index(columnList = "expandable,expanded"),})
@Data
@LogicDeleteEntity(activeFlagValues = {0, 1})
public class DynamicFormConfigrationEntity implements IIdentifiable<Long>, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Comment("主键ID（自增）")
    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    /**
     * 表单字段是否展开（0:收起，1:展开）
     */
    @Comment("表单字段是否展开（0:收起，1:展开）")
    @Column(name = "expanded")
    private Integer expanded;

    /**
     * 是否可展开（0:不可展开，1:可展开）
     */
    @Comment("表单是否支持展开/收起（0:不可，1:可以）")
    @Column(name = "expandable")
    private Integer expandable;

    /**
     * 表单字段的间隔
     */
    @Comment("表单字段之间的间隔（像素值）")
    @Column(name = "gutter")
    private Integer gutter;

    /**
     * 表单字段的宽度
     */
    @Comment("表单标签宽度（如 '100px'）")
    @Column(name = "label_width")
    private String labelWidth;

    /**
     * 表单栅格占据的列数
     */
    @Comment("表单栅格占据的列数（基于 24 栅格系统）")
    @Column(name = "span")
    private Integer span;

    /**
     * 表单标题
     */
    @Comment("表单标题")
    @Column(name = "title", length = 128)
    private String title;

    /**
     * 标记
     * @see com.tbs.shared.enums.DynamicFormConfigFlagEnum
     */
    @Comment("数据状态标志（逻辑删除用）")
    @Column(name = "flag")
    private Integer flag;

}
