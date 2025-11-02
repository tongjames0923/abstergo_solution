package com.tbs.struct.tree.impl.model;

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
    /**
     * 树组
     */
    private String treeGroup;
    /**
     * 节点ID
     */
    private String nodeId;
}
