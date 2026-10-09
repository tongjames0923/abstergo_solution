package com.tbs.struct.tree.closure.impl.entity;

import lombok.Data;
import org.hibernate.annotations.Comment;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 树节点实体类
 *
 * @author abstergo
 */

@Data
@Entity
@Table(name = "tree_node", uniqueConstraints = @UniqueConstraint(columnNames = {"group_name", "node_id"}),
    indexes = {@jakarta.persistence.Index(columnList = "group_name,parent_id"),
        @jakarta.persistence.Index(columnList = "entity_id")})
@Comment("树节点表（闭包式树结构的节点主档）")
public class TreeNodeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Comment("主键ID（自增）")
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 节点ID
     */

    @Comment("节点ID（树内唯一标识）")
    @Column(name = "node_id", length = 64)
    private String nodeId;

    /**
     * 树组名称,作为树的id
     */
    @Comment("树组名称（作为树的唯一标识，同一树组内节点ID 不重复）")
    @Column(name = "group_name", length = 64)
    private String groupName;

    /**
     * 父节点ID,作为关系备份，若闭包表关系不可靠时，可恢复，null表示根节点
     */
    @Comment("父节点ID（关系备份，闭包关系不可靠时可恢复；null 表示根节点）")
    @Column(name = "parent_id", length = 64)
    private String parentId;

    /**
     * 创建时间
     */
    @Comment("创建时间")
    @Column(name = "create_time")
    private Date createTime;
    /**
     * 修改时间
     */
    @Comment("修改时间")
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 实体ID
     */
    @Comment("节点对应的业务实体ID")
    @Column(name = "entity_id")
    private Long entityId;

}
