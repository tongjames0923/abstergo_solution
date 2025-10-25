package com.tbs.struct.tree.api;

import java.util.List;

/**
 * 树节点接口
 *
 * @param <K>
 * @param <V>
 * @author abstergo
 */
public interface ITreeNode<K, V> {
    /**
     * 获取节点的key
     *
     * @return
     */
    K getKey();

    /**
     * 获取节点的值
     *
     * @return
     */
    V getValue();

    /**
     * 设置节点的值
     *
     * @param value
     */
    void setValue(V value);

    /**
     * 获取子节点
     *
     * @return
     */
    List<ITreeNode<K, V>> getChildren();

    /**
     * 设置子节点
     *
     * @param children
     */
    void setChildren(List<ITreeNode<K, V>> children);
}
