package com.tbs.shared.interfaces.service;

import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.struct.tree.api.persistence.ITreeStorable;

import java.util.List;

/**
 * 树结构服务接口
 *
 * @author abstergo
 */
public interface ITreeStructService<E extends IIdentifiable<ID>, ID> {

    /**
     * 根据ID获取树实体的子树
     *
     * @param id
     * @return
     */
    public List<ITreeStorable> getChildren(ID id);

    /**
     * 根据ID获取树结构数据
     *
     * @param id
     * @return
     */
    public ITreeStorable getNode(ID id);

    /**
     * 根据ID获取树实体的直接子树
     *
     * @param id
     * @return
     */
    public List<E> getDirectChildren(ID id);

    /**
     * 删除叶子
     *
     * @param id
     * @return
     */
    public boolean deleteLeaf(ID id);

    /**
     * 判断是否是叶子
     *
     * @param id
     * @return
     */
    public boolean isLeaf(ID id);

    /**
     * 判断是否是根
     *
     * @param id
     * @return
     */
    public boolean isRoot(ID id);

    /**
     * 设置为树,若parent为空则设置为根树
     *
     * @param data
     * @param parent
     */
    public void setToTree(ID data, ID parent);

    /**
     * 根据节点ID查询
     *
     * @param nodeId
     * @param group
     * @return
     */

    E queryByNodeId(String nodeId, String group);
}
