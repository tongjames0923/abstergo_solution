package com.tbs.struct.tree.api.persistence.impl;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.struct.tree.api.persistence.ITreeStorable;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import com.tbs.struct.tree.model.TreeNodeModel;

import java.util.List;

/**
 * 树存储接口实现类
 *
 */
public class NoImplTreeStore implements ITreeStore {

    public NoImplTreeStore() {
        throw new FormattedRuntimeException("树类未实现");
    }

    @Override
    public void store(ITreeStorable treeStorable, String parentNodeId) {

    }

    @Override
    public List<ITreeStorable> getDirectChildren(String nodeId, String group) {
        return List.of();
    }

    @Override
    public List<TreeNodeModel> getParentNodes(String nodeId, String group) {
        return List.of();
    }

    @Override
    public void delete(String nodeId, String group) {

    }

    @Override
    public List<ITreeStorable> getRootNodes(String treeGroup) {
        return List.of();
    }

    @Override
    public List<String> listTreeGroups() {
        return List.of();
    }

    @Override
    public boolean isExist(String nodeId, String group) {
        return false;
    }

    @Override
    public void tryRebuildStructure(String treeGroup) {

    }

    @Override
    public ITreeStorable getNode(String nodeId, String group) {
        return null;
    }

    @Override
    public List<ITreeStorable> getNodeByEntityId(Long id) {
        return List.of();
    }
}
