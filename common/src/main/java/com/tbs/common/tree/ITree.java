package com.tbs.common.tree;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 树接口
 *
 * @author abstergo
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface ITree<K, V> {
    /**
     * 获取根节点
     *
     * @return 根节点
     */
    ITreeNode<K, V> getRoot(); // 获取根节点

    /**
     * 设置根节点
     *
     * @param root 根节点
     */
    void setRoot(ITreeNode<K, V> root); // 设置根节点

    /**
     * 获取树的节点总数
     *
     * @return 节点总数
     */
    int size(); // 获取树的节点总数

    /**
     * 判断树是否为空
     *
     * @return true:为空 false:不为空
     */
    boolean isEmpty(); // 判断树是否为空

    /**
     * 遍历节点
     *
     * @param visitor
     */
    void traverse(Consumer<ITreeNode<K, V>> visitor); // 遍历树

    /**
     * 按键查找节点
     *
     * @param key
     * @return
     */
    Optional<ITreeNode<K, V>> find(K key); // 按键查找节点

    /**
     * 获取树的高度
     *
     * @return
     */
    int height();

    /**
     * 获取叶子节点
     *
     * @return
     */
    List<ITreeNode<K, V>> getLeafNodes();

    /**
     * 获取指定深度的节点
     *
     * @param depth
     * @return
     */
    List<ITreeNode<K, V>> getNodesByDepth(int depth);

    /**
     * 插入新节点
     *
     * @param parentKey
     * @param newNode
     */
    void insert(K parentKey, ITreeNode<K, V> newNode); // 插入新节点到指定父节点下

    /**
     * 删除节点
     *
     * @param key
     * @return
     */
    boolean delete(K key); // 删除指定节点
}
