package com.tbs.struct.tree.closure.impl.entity;

import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Data
@Table(name = "tree_closure", uniqueConstraints = {@UniqueConstraint(columnNames = {"ancestor_id", "descendant_id"})},
    indexes = {@Index(columnList = "ancestor_id"), @Index(columnList = "descendant_id"), @Index(columnList = "depth"),
        @Index(columnList = "group_name")})
@Entity
public class TreeRelationEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @Column(name = "id")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 祖先节点ID
     */
    @Column(name = "ancestor_id", length = 64, nullable = false)
    private String ancestorId;

    /**
     * 后代节点ID
     */
    @Column(name = "descendant_id", length = 64, nullable = false)
    private String descendantId;

    /**
     * 与nodeId与ancestorId的距离（depth） 0表示自己到自己(nodeId==ancestorId) 1表示父子关系 2表示祖孙关系等
     */
    @Column(name = "depth", nullable = false)
    private Integer depth;

    /**
     * 树组名称
     */
    @Column(name = "group_name", length = 64, nullable = false)
    private String groupName;

}
