package com.tbs.shared.pojo.entity.view;

import com.tbs.shared.pojo.IIdentifiable;
import org.hibernate.annotations.Comment;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 表单域配置表
 *
 * <p>定义表单中每个字段（域）的类型、属性名和标签等配置信息。</p>
 *
 * @author abstergo
 */
@Entity
@Table(name = "form_field_configration", indexes = {@Index(name = "gobal_index", columnList = "type,prop,label")})
@Data
public class FormFieldConfigrationEntity implements Serializable, IIdentifiable<Long> {
    private static final long serialVersionUID = 1L;

    /**
     * 表单域主键
     */
    @Comment("主键ID（自增）")
    @Id
    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;

    /**
     * 表单域组件类型
     */
    @Comment("表单域组件类型（如 input、select、date 等）")
    @Column(name = "type", length = 64)
    private String type;

    /**
     * 表单域对应属性名称
     */
    @Comment("表单域对应的数据属性名称（绑定字段名）")
    @Column(name = "prop", length = 64)
    private String prop;

    /**
     * 表单域标签
     */
    @Comment("表单域标签（显示在表单中的字段名称）")
    @Column(name = "label", length = 64)
    private String label;

}
