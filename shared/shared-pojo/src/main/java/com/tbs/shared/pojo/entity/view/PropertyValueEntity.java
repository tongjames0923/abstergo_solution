package com.tbs.shared.pojo.entity.view;

import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

/**
 * 属性设置表
 *
 * <p>存储动态表单中各组件的属性配置，支持通过转换器将属性值转换为对应类型。</p>
 *
 * @author abstergo
 */
@Data
@Entity
@Table(name = "property_value", indexes = {@Index(name = "name_idx", columnList = "prop_name"),})
public class PropertyValueEntity implements IIdentifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Column(name = "id")
    @jakarta.persistence.Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
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
