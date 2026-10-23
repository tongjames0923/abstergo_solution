package com.tbs.struct.tree.closure.impl.entity;

import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import lombok.Data;
import org.hibernate.annotations.Comment;

import jakarta.persistence.*;
import java.io.Serializable;

@Data
@Table(name = "tree_closure", uniqueConstraints = {@UniqueConstraint(columnNames = {"ancestor_id", "descendant_id"})},
    indexes = {@Index(columnList = "ancestor_id"), @Index(columnList = "descendant_id"), @Index(columnList = "depth"),
        @Index(columnList = "group_name")})
@Entity
@Comment("树闭包关系表（存储任意祖先-后代关系对）")
public class TreeRelationEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @Comment("主键（雪花算法生成）")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 祖先节点ID
     */
    @Comment("祖先节点ID")
    @Column(name = "ancestor_id", length = 64, nullable = false)
    private String ancestorId;

    /**
     * 后代节点ID
     */
    @Comment("后代节点ID")
    @Column(name = "descendant_id", length = 64, nullable = false)
    private String descendantId;

    /**
     * 与nodeId与ancestorId的距离（depth） 0表示自己到自己(nodeId==ancestorId) 1表示父子关系 2表示祖孙关系等
     */
    @Comment("层级深度（0 为自身，1 为父子，2 为祖孙，依次类推）")
    @Column(name = "depth", nullable = false)
    private Integer depth;

    /**
     * 树组名称
     */
    @Comment("树组名称（作为树的唯一标识）")
    @Column(name = "group_name", length = 64, nullable = false)
    private String groupName;

}
