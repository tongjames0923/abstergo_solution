package com.tbs.struct.tree.api.persistence;

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
    void store(ITreeStorable treeStorable, String parentTreeId);

    /**
     * 获取树
     *
     * @param treeId 树ID
     * @return 树存储对象
     */
    List<ITreeStorable> getChildren(String treeId);

    /**
     * 获取树父节点
     *
     * @param treeId 树ID
     * @return 树存储对象
     */
    ITreeStorable getTreeParent(String treeId);

    /**
     * 删除树
     *
     * @param treeId 树ID
     */
    void delete(String treeId);

    /**
     * 批量删除树
     *
     * @param treeIds 树ID列表
     */
    void batchDelete(List<String> treeIds);

    /**
     * 批量存储树
     *
     * @param nodes 树存储对象列表
     */
    void batchStore(List<ITreeStorable> nodes);

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

}
