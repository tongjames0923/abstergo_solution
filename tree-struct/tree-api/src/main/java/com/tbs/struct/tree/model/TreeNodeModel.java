package com.tbs.struct.tree.model;

import com.tbs.struct.tree.api.persistence.ITreeStorable;
import lombok.Data;

import java.io.Serializable;

/**
 * 树节点模型
 *
 * @author tbs
 */
@Data
public class TreeNodeModel implements ITreeStorable, Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 实体ID
     */
    private Long entityId;
    /**
     * 节点ID
     */
    private String nodeId;
    /**
     * 节点层级,距离根节点的层数
     */
    private int depth;

    /**
     * 树组
     */
    private String treeGroup;
}
