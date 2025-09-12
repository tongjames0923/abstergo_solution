package com.tbs.shared.pojo.entity.view;

import com.tbs.shared.pojo.IIdentifiable;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 表单域配置表
 */
@Entity
@Table(name = "form_field_configration")
@Data
public class FormFieldConfigrationEntity implements Serializable, IIdentifiable<Long> {
    private static final long serialVersionUID = 1L;
    /**
     * 表单域主键
     */
    @Id
    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;
    /**
     * 表单域组件类型
     */
    @Column(name = "type", length = 64)
    private String type;
    /**
     * 表单域对应属性名称
     */
    @Column(name = "prop", length = 64)
    private String prop;
    /**
     * 表单域标签
     */
    @Column(name = "label", length = 64)
    private String label;
    /**
     * 表单域占位符
     */
    @Column(name = "placeholder", length = 128)
    private String placeholder;
    /**
     * 表单域是否可清空
     */
    @Column(name = "clearable")
    private Integer clearable;
    /**
     * 表单域前缀图标
     */
    @Column(name = "prefix_icon", length = 64)
    private String prefixIcon;

}
