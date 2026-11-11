package com.tbs.struct.tree.closure.impl.entity;

import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.*;
import lombok.Data;

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
public class TreeNodeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id
    @Column(name = "id")
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 节点ID
     */

    @Column(name = "node_id", length = 64)
    private String nodeId;

    /**
     * 树组名称,作为树的id
     */
    @Column(name = "group_name", length = 64)
    private String groupName;

    /**
     * 父节点ID,作为关系备份，若闭包表关系不可靠时，可恢复，null表示根节点
     */
    @Column(name = "parent_id", length = 64)
    private String parentId;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;
    /**
     * 修改时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 实体ID
     */
    @Column(name = "entity_id")
    private Long entityId;

}
