package com.tbs.common.tree;

public abstract class AbstractTree<K, V> implements ITree<K, V> {
    protected ITreeNode<K, V> root;
    protected int size;

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

    // 默认的插入逻辑（子类可以覆盖）
    @Override
    public void insert(K parentKey, ITreeNode<K, V> newNode) {
        if (root == null) {
            root = newNode;
            size++;
            return;
        }
        insertNode(root, parentKey, newNode);
    }

    protected abstract void insertNode(ITreeNode<K, V> currentNode, K parentKey, ITreeNode<K, V> newNode);

    // 默认的删除逻辑（子类可以覆盖）
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

    protected abstract boolean deleteNode(ITreeNode<K, V> parent, ITreeNode<K, V> currentNode, K key);
}
