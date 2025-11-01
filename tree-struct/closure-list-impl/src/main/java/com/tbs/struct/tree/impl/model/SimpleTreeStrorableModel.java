package com.tbs.struct.tree.impl.model;

import com.tbs.struct.tree.api.persistence.ITreeStorable;
import lombok.Data;

import java.io.Serializable;

/**
 * 简单树节点模型
 *
 * @author tbs
 */
@Data
public class SimpleTreeStrorableModel implements ITreeStorable, Serializable {
    /**
     * 树组
     */
    private String treeGroup;
    /**
     * 节点ID
     */
    private String nodeId;
}
