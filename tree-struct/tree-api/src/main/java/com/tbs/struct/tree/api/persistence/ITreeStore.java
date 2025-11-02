package com.tbs.struct.tree.api.persistence;

import com.tbs.struct.tree.model.TreeNodeModel;

import java.util.List;

/**
 * 树存储接口
 *
 * @author tbs
 * @date 2022/3/23 11:05
 */
public interface ITreeStore {
    /**
     * 存储树
     *
     * @param treeStorable 树存储对象
     */
    void store(ITreeStorable treeStorable, String parentNodeId);

    /**
     * 获取树
     *
     * @param nodeId 树ID
     * @return 树存储对象
     */
    List<ITreeStorable> getChildren(String nodeId);

    /**
     * 获取树父节点
     *
     * @param nodeId 树ID
     * @return 树存储对象
     */
    List<TreeNodeModel> getParentNodes(String nodeId);

    /**
     * 删除树
     *
     * @param nodeId 树ID
     */
    void delete(String nodeId);

    /**
     * 批量删除树
     *
     * @param nodeIds 树ID列表
     */
    void batchDelete(List<String> nodeIds);

    /**
     * 获取根节点
     *
     * @param treeGroup 树组
     * @return 树存储对象列表
     */
    List<ITreeStorable> getRootNodes(String treeGroup);

    /**
     * 获取所有树组
     *
     * @return 树组列表
     */
    List<String> listTreeGroups();

    /**
     * 判断树是否存在
     *
     * @param nodeId 树ID
     * @return 树是否存在
     */
    boolean isExist(String nodeId);

}
