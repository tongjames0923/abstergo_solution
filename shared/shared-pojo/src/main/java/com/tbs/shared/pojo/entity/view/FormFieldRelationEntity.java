package com.tbs.shared.pojo.entity.view;

import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

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
    indexes = {@jakarta.persistence.Index(name = "form_field_relation_form_id_index", columnList = "form_id")})
public class FormFieldRelationEntity implements IIdentifiable<Long>, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Column(name = "id")
    @jakarta.persistence.Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 表单ID
     */
    @Column(name = "form_id")
    private Long formId;

    /**
     * 表单域Id
     */
    @Column(name = "field_id")
    private Long fieldId;

}
