package com.tbs.struct.tree.api.persistence;

/**
 * 支持储存的树节点接口
 */
public interface ITreeStorable {
    /**
     * 设置树组
     *
     * @param group
     */
    void setTreeGroup(String group);

    /**
     * 获取树组
     *
     * @return
     */
    String getTreeGroup();

    /**
     * 获取树ID
     *
     * @return
     */
    String getTreeId();

    /**
     * 设置树ID
     *
     * @param treeId
     */
    void setTreeId(String treeId);

}
