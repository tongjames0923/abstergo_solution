package com.tbs.shared.pojo;

/**
 * 树结构模型接口
 *
 * @param <ID>
 * @author abstergo
 */
public interface ITreeStructModel<ID> {
    /**
     * 获取树组名称
     *
     * @return
     */
    public String getTreeNodeGroup();

    /**
     * 获取树节点ID
     *
     * @return
     */
    public String getTreeNodeId();

    /**
     * 获取树根标识
     *
     * @return
     */
    public Integer getTreeRootFlag();

    /**
     * 设置树节点ID
     *
     * @param treeNodeId
     */
    public void setTreeNodeId(String treeNodeId);

    /**
     * 设置树组名称
     *
     * @param treeGroupName
     */
    public void setTreeNodeGroup(String treeGroupName);

    /**
     * 设置树根标识
     *
     * @param treeRootFlag
     */
    public void setTreeRootFlag(Integer treeRootFlag);

}
