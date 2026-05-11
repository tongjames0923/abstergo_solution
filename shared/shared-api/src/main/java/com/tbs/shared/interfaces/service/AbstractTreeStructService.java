package com.tbs.shared.interfaces.service;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;

import com.tbs.shared.pojo.ITreeStructModel;
import com.tbs.struct.tree.api.persistence.ITreeStorable;
import com.tbs.struct.tree.api.persistence.ITreeStore;

import com.tbs.struct.tree.model.TreeNodeModel;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractTreeStructService<E extends ITreeStructModel<ID>, ID>
    implements ITreeStructService<E, ID> {

    /**
     * 获取树存储接口
     *
     * @return
     */
    protected abstract ITreeStore getTreeStore();

    /**
     * 获取树节点ID
     *
     * @param d
     * @return
     */
    protected abstract String getGroupName(ID d);

    /**
     * 获取树节点ID
     *
     * @param d
     * @param e
     * @return
     */
    protected abstract String getNodeId(ID d, E e);

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    protected abstract E queryById(ID id);

    /**
     * 更新
     *
     * @param id
     * @param e
     * @return
     */
    protected abstract E update(ID id, E e);

    protected E preCheck(ID id) {
        String group = getGroupName(id);
        var e = queryById(id);
        AssertUtil.isTrue(e == null, "未找到数据实体");
        if (StrUtil.isNotEmpty(e.getTreeNodeGroup()) && StrUtil.isEmpty(e.getTreeNodeId())) {
            e.setTreeNodeId(getNodeId(id, e));
            update(id, e);
        }
        return e;
    }

    @Override
    public List<ITreeStorable> getChildren(ID id) {
        var e = preCheck(id);
        return getTreeStore().getDirectChildren(e.getTreeNodeId(), e.getTreeNodeGroup());
    }

    @Override
    public List<E> getDirectChildren(ID id) {
        var e = preCheck(id);
        return getTreeStore().getDirectChildren(e.getTreeNodeId(), e.getTreeNodeGroup()).stream()
            .map(it -> queryByNodeId(it.getNodeId(), it.getTreeGroup())).collect(Collectors.toList());
    }

    @Override
    public boolean deleteLeaf(ID id) {
        var e = preCheck(id);
        var children = getTreeStore().getDirectChildren(e.getTreeNodeId(), e.getTreeNodeGroup());
        if (CollUtil.isNotEmpty(children)) {
            return false;
        }

        getTreeStore().delete(e.getTreeNodeId(), e.getTreeNodeGroup());

        return true;
    }

    @Override
    public void setToTree(ID id, ID p) {

        var source = queryById(id);
        AssertUtil.isTrue(source != null, "数据不存在");
        var parent = queryById(p);
        if (StrUtil.isEmpty(parent.getTreeNodeGroup())) {
            parent.setTreeNodeGroup(getGroupName(p));
            parent.setTreeNodeId(getNodeId(p, parent));
            var tree = new TreeNodeModel();
            tree.setTreeGroup(parent.getTreeNodeGroup());
            tree.setNodeId(parent.getTreeNodeId());
            getTreeStore().store(tree, null);
        }
        AssertUtil.isTrue(StrUtil.isNotEmpty(source.getTreeNodeGroup()) &&
                !Objects.equals(source.getTreeNodeGroup(), parent.getTreeNodeGroup())).message("不可跨树移动")
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        if (StrUtil.isEmpty(source.getTreeNodeGroup())) {
            source.setTreeNodeGroup(parent.getTreeNodeGroup());
            source.setTreeNodeId(getNodeId(id, source));
            source.setTreeRootFlag(0);
            update(id, source);
        }
        getTreeStore().store(new TreeNodeModel(source.getTreeNodeId(), source.getTreeNodeGroup()),
            parent.getTreeNodeId());

        update(id, source);

    }

    @Override
    public boolean isLeaf(ID id) {
        var e = preCheck(id);
        var children = getTreeStore().getDirectChildren(e.getTreeNodeId(), e.getTreeNodeGroup());
        return CollUtil.isEmpty(children);
    }

    @Override
    public boolean isRoot(ID id) {
        var e = preCheck(id);
        return Objects.equals(e.getTreeRootFlag(), Integer.valueOf(1));
    }

    @Override
    public ITreeStorable getNode(ID id) {
        var e = preCheck(id);
        return getTreeStore().getNode(e.getTreeNodeId(), e.getTreeNodeGroup());
    }
}
