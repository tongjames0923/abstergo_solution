package com.tbs.shared.pojo.entity.view;

import com.tbs.shared.pojo.IIdentifiable;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 表单与表单域关联表
 */
@Data
@Entity
@Table(name = "form_field_relation",
    indexes = {@javax.persistence.Index(name = "form_field_relation_form_id_index", columnList = "form_id")})
public class FormFieldRelationEntity implements IIdentifiable<Long>, Serializable {
    private static final long serialVersionUID = 1L;
    @javax.persistence.Id
    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;

    /**
     * 表单ID
     */
    private Long formId;

    /**
     * 表单域Id
     */
    private Long fieldId;

}
