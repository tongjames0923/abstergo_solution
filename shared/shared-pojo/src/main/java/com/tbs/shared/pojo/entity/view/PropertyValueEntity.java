package com.tbs.shared.pojo.entity.view;

import com.tbs.shared.pojo.IIdentifiable;
import org.hibernate.annotations.Comment;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
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
    @Comment("主键ID（自增）")
    @javax.persistence.Id
    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;

    /**
     * 属性名称
     */
    @Comment("属性名称（如 placeholder、disabled、rules 等）")
    @Column(name = "prop_name", length = 64)
    private String name;

    /**
     * 属性值
     */
    @Comment("属性值（JSON 或纯文本格式）")
    @Column(name = "prop_value", columnDefinition = "text")
    private String value;

    /**
     * 属性值转换器类型,通过SpeingBean获取实例，将属性值转换成对应的类型
     */
    @Comment("属性值转换器类型（通过 Spring Bean 名称获取实例，进行类型转换）")
    @Column(name = "transformer_type", length = 64)
    private String transformerType;

    /**
     * 属性值对应的类
     */
    @Comment("属性值对应的目标类全限定名（用于反序列化）")
    @Column(name = "value_class", length = 256)
    private String valueClass;
}
