package com.tbs.shared.pojo.entity.view;

import com.tbs.shared.pojo.IIdentifiable;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 属性设置表
 */
@Data
@Entity
@Table(name = "property_value")
public class PropertyValueEntity implements IIdentifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;
    @javax.persistence.Id
    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;

    /**
     * 属性名称
     */
    @Column(name = "prop_name", length = 64)
    private String name;

    /**
     * 属性值
     */
    @Column(name = "prop_value", columnDefinition = "text")
    private String value;

    /**
     * 属性值转换器类型,通过SpeingBean获取实例，将属性值转换成对应的类型
     */
    @Column(name = "transformer_type", length = 64)
    private String transformerType;

    /**
     * 属性值对应的类
     */
    @Column(name = "value_class", length = 256)
    private String valueClass;
}
