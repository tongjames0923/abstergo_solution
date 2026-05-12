package com.tbs.shared.interfaces.service;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.struct.tree.api.persistence.ITreeStorable;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import com.tbs.struct.tree.model.TreeNodeModel;

import java.util.List;

/**
 * 抽象树状结构服务
 *
 * @param <E>
 * @param <Long>
 * @author abstergo
 */
public abstract class AbstractTreeStructService<E extends IIdentifiable<Long>> implements ITreeStructService {

    /**
     * 获取树存储接口
     *
     * @return
     */
    protected abstract ITreeStore<Long> getTreeStore();

    /**
     * 获取树节点ID
     *
     * @param d
     * @return
     */
    protected abstract String getGroupName();

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    protected abstract E queryById(Long id);

    /**
     * 获取树节点ID
     *
     * @param d
     * @param e
     * @return
     */
    protected abstract String getNodeId(Long d, E e);

    ITreeStorable getTreeNode(Long id) {
        var s = getTreeStore().getNodeByEntityIdAndGroup(id, getGroupName());
        if (s != null) {
            return s;
        }
        return null;
    }

    @Override
    public List<ITreeStorable> getChildren(Long id) {
        var e = getTreeNode(id);
        if (e == null) {
            return List.of();
        }
        return getTreeStore().getDirectChildren(e.getNodeId(), e.getTreeGroup());
    }

    @Override
    public boolean deleteLeaf(Long id) {
        var e = getTreeNode(id);
        if (e == null) {
            return false;
        }
        var children = getTreeStore().getDirectChildren(e.getNodeId(), e.getTreeGroup());
        if (CollUtil.isNotEmpty(children)) {
            return false;
        }

        getTreeStore().delete(e.getNodeId(), e.getTreeGroup());

        return true;
    }

    @Override
    public void setToTree(Long id, Long p) {
        var ce = queryById(id);
        AssertUtil.isTrue(ce == null, "数据不存在");
        ITreeStorable parent = null;
        if (p != null) {
            var pe = queryById(p);
            AssertUtil.isTrue(pe == null, "数据不存在");
            parent = getTreeNode(p);
            // 如果父节点没有树信息，先创建父节点的树节点
            if (parent == null) {
                parent = new TreeNodeModel(getNodeId(p, pe), getGroupName(), p);
                getTreeStore().store(parent, null);
            }
        }
        var group = parent == null ? getGroupName() : parent.getTreeGroup();
        var source = getTreeNode(id);
        if (source == null) {
            source = new TreeNodeModel(getNodeId(id, ce), group, id);
        }
        getTreeStore().store(source, parent == null ? null : parent.getNodeId());
    }

    @Override
    public boolean isLeaf(Long id) {
        var e = getTreeNode(id);
        var children = getTreeStore().getDirectChildren(e.getNodeId(), e.getTreeGroup());
        return CollUtil.isEmpty(children);
    }

    @Override
    public boolean isRoot(Long id) {
        var e = getTreeNode(id);
        var p = getTreeStore().getParentNodes(e.getNodeId(), e.getTreeGroup());
        return CollUtil.isNotEmpty(p);
    }

    @Override
    public ITreeStorable getNode(Long id) {
        return getTreeNode(id);
    }
}
