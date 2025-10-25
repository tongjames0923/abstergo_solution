package com.tbs.struct.tree.api;

/**
 * 抽象树结构
 *
 * @param <K>
 * @param <V>
 * @author abstergo
 */
public abstract class AbstractTree<K, V> implements ITree<K, V> {
    private ITreeNode<K, V> root;
    private int size;

    /**
     * 设置树大小
     *
     * @param s
     */
    protected void setSize(int delta) {
        size += delta;
    }

    @Override
    public ITreeNode<K, V> getRoot() {
        return root;
    }

    @Override
    public void setRoot(ITreeNode<K, V> root) {
        this.root = root;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 插入节点
     *
     * @param parentKey
     * @param newNode
     */
    @Override
    public void insert(K parentKey, ITreeNode<K, V> newNode) {
        if (root == null) {
            root = newNode;
            size++;
            return;
        }
        insertNode(root, parentKey, newNode);
    }

    /**
     * 插入节点
     *
     * @param currentNode
     * @param parentKey
     * @param newNode
     */
    protected abstract void insertNode(ITreeNode<K, V> currentNode, K parentKey, ITreeNode<K, V> newNode);

    /**
     * 删除节点
     *
     * @param key
     * @return
     */
    @Override
    public boolean delete(K key) {
        if (root == null) {
            return false;
        }
        boolean deleted = deleteNode(null, root, key);
        if (deleted) {
            size--;
        }
        return deleted;
    }

    /**
     * 删除节点
     *
     * @param parent
     * @param currentNode
     * @param key
     * @return
     */
    protected abstract boolean deleteNode(ITreeNode<K, V> parent, ITreeNode<K, V> currentNode, K key);

}
