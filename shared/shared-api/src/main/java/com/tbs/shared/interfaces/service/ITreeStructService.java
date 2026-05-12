package com.tbs.shared.interfaces.service;

import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.struct.tree.api.persistence.ITreeStorable;

import java.util.List;

/**
 * 树结构服务接口
 *
 * @author abstergo
 */
public interface ITreeStructService<E extends IIdentifiable<Long>> {

    /**
     * 根据ID获取树实体的直接子节点
     *
     * @param id
     * @return
     */
    public List<ITreeStorable> getChildren(Long id);

    /**
     * 根据ID获取树结构数据
     *
     * @param id
     * @return
     */
    public ITreeStorable getNode(Long id);

    /**
     * 删除叶子
     *
     * @param id
     * @return
     */
    public boolean deleteLeaf(Long id);

    /**
     * 判断是否是叶子
     *
     * @param id
     * @return
     */
    public boolean isLeaf(Long id);

    /**
     * 判断是否是根
     *
     * @param id
     * @return
     */
    public boolean isRoot(Long id);

    /**
     * 设置为树,若parent为空则设置为根树
     *
     * @param data
     * @param parent
     */
    public void setToTree(Long data, Long parent);

    /**
     * 根据节点ID查询
     *
     * @param nodeId
     * @param group
     * @return
     */

    E queryByNodeId(String nodeId, String group);
}
