package com.tbs.shared.pojo.entity.view;

import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

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
        @jakarta.persistence.Index(columnList = "expandable,expanded")})
@Data
@LogicDeleteEntity(activeFlagValues = {0, 1})
public class DynamicFormConfigrationEntity implements IIdentifiable<Long>, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Column(name = "id")
    @jakarta.persistence.Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 表单字段是否展开（0:收起，1:展开）
     */
    @Column(name = "expanded")
    private Integer expanded;

    /**
     * 是否可展开（0:不可展开，1:可展开）
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

    /**
     * 表单标题
     */
    @Column(name = "title", length = 128)
    private String title;

    /**
     * 标记
     * @see com.tbs.shared.enums.DynamicFormConfigFlagEnum
     */
    @Column(name = "flag")
    private Integer flag;

}
