package com.tbs.struct.tree.impl.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Table(name = "tree_closure", uniqueConstraints = {@UniqueConstraint(columnNames = {"ancestor_id", "descendant_id"})})
@Entity
public class TreeRelationEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

}
