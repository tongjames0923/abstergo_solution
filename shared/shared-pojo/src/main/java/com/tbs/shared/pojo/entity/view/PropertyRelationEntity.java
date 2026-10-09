package com.tbs.shared.pojo.entity.view;

import com.tbs.shared.pojo.IIdentifiable;
import org.hibernate.annotations.Comment;
import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * 属性关系表
 *
 * <p>维护属性与表单域/表单之间的关联关系，支持将属性绑定到不同的实体类型。</p>
 *
 * @author abstergo
 * @see PropertyValueEntity
 */
@Data
@Entity
@Table(name = "property_relation",
    indexes = {@jakarta.persistence.Index(name = "property_relation_property_id_index", columnList = "property_id"),
        @jakarta.persistence.Index(name = "property_relation_relation_id_index",
            columnList = "relation_id,relation_type")})
public class PropertyRelationEntity implements IIdentifiable<Long>, Serializable {

    /**
     * 主键ID
     */
    @Comment("主键ID（自增）")
    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    /**
     * 属性id
     */
    @Comment("属性ID（援引 property_value.id）")
    @Column(name = "property_id")
    private Long propertyId;

    /**
     * 关联类型， 1-字段， 2-表单
     */
    @Comment("关联类型（1:绑定到字段，2:绑定到表单）")
    @Column(name = "relation_type")
    private Integer relationType;

    /**
     * 关联id
     */
    @Comment("关联对象ID（根据 relation_type 指向表单域ID或表单ID）")
    @Column(name = "relation_id")
    private Long relationId;
}
