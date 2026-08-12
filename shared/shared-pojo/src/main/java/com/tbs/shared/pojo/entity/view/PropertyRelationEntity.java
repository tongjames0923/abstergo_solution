package com.tbs.shared.pojo.entity.view;

import com.tbs.shared.pojo.IIdentifiable;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 属性关系表
 */
@Data
@Entity
@Table(name = "property_relation",
    indexes = {@javax.persistence.Index(name = "property_relation_property_id_index", columnList = "property_id"),
        @javax.persistence.Index(name = "property_relation_relation_id_index",
            columnList = "relation_id,relation_type")})
public class PropertyRelationEntity implements IIdentifiable<Long>, Serializable {
    @javax.persistence.Id
    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id")
    /**
     * 属性id
     */ private Long propertyId;
    /**
     * 关联类型， 1-字段， 2-表单
     *
     */
    @Column(name = "relation_type")
    private Integer relationType;

    /**
     * 关联id
     */
    @Column(name = "relation_id")
    private Long relationId;
}
