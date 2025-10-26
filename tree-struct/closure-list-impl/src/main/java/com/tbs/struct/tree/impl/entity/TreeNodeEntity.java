package com.tbs.struct.tree.impl.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 树节点实体类
 *
 * @author abstergo
 */

@Data
@Entity
@Table(name = "tree_node", indexes = {@javax.persistence.Index(columnList = "group_name,parent_id")})
public class TreeNodeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    @Id
    @Column(name = "id", length = 64)
    private String id;

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

}
