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

}
