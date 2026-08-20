package com.tbs.shared.pojo.entity.view;

import com.tbs.shared.pojo.IIdentifiable;
import org.hibernate.annotations.Comment;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 表单与表单域关联表
 *
 * <p>维护表单配置与表单域配置之间的多对多关联关系。</p>
 *
 * @author abstergo
 * @see DynamicFormConfigrationEntity
 * @see FormFieldConfigrationEntity
 */
@Data
@Entity
@Table(name = "form_field_relation",
    indexes = {@javax.persistence.Index(name = "form_field_relation_form_id_index", columnList = "form_id")})
public class FormFieldRelationEntity implements IIdentifiable<Long>, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Comment("主键ID（自增）")
    @javax.persistence.Id
    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;

    /**
     * 表单ID
     */
    @Comment("表单配置ID（援引 dynamic_form_configration.id）")
    @Column(name = "form_id")
    private Long formId;

    /**
     * 表单域Id
     */
    @Comment("表单域配置ID（援引 form_field_configration.id）")
    @Column(name = "field_id")
    private Long fieldId;

}
