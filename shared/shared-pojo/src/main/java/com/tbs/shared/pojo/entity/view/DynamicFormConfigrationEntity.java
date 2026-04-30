package com.tbs.shared.pojo.entity.view;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.IIdentifiable;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 表单配置
 */
@Entity
@Table(name = "dynamic_form_configration",
    indexes = {@javax.persistence.Index(columnList = "flag"), @javax.persistence.Index(columnList = "title"),
        @javax.persistence.Index(columnList = "expandable,expanded"),})
@Data
@LogicDeleteEntity(activeFlagValues = {0, 1})
public class DynamicFormConfigrationEntity implements IIdentifiable<Long>, Serializable {
    private static final long serialVersionUID = 1L;
    @javax.persistence.Id
    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;
    /**
     * 表单字段
     */
    @Column(name = "expanded")
    private Integer expanded;
    /**
     * 是否可展开
     */
    @Column(name = "expandable")
    private Integer expandable;
    /**
     * 表单字段的间隔
     */
    @Column(name = "gutter")
    private Integer gutter;
    /**
     * 表单字段的宽度
     */
    @Column(name = "label_width")
    private String labelWidth;
    /**
     * 表单栅格占据的列数
     */
    @Column(name = "span")
    private Integer span;

    @Column(name = "title", length = 128)
    private String title;

    /**
     * 标记
     * @see com.tbs.shared.enums.DynamicFormConfigFlagEnum
     */
    @Column(name = "flag")
    private Integer flag;

}
