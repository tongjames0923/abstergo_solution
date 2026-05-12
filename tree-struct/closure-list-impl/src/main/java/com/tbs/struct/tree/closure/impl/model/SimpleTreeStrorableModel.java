package com.tbs.struct.tree.closure.impl.model;

import com.tbs.struct.tree.api.persistence.ITreeStorable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 简单树节点模型
 *
 * @author tbs
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleTreeStrorableModel implements ITreeStorable, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 树组
     */
    private String treeGroup;
    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 实体ID
     */
    private Long entityId;
}
