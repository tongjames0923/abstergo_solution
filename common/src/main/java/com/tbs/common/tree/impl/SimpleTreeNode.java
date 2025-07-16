package com.tbs.common.tree.impl;

import com.tbs.common.tree.ITreeNode;

import java.util.LinkedList;
import java.util.List;

public class SimpleTreeNode<K, V> implements ITreeNode<K, V> {
    private K key;
    private V value;

    private List<ITreeNode<K, V>> children = new LinkedList<>();

    public SimpleTreeNode(K key, V value, List<ITreeNode<K, V>> children) {
        this.key = key;
        this.value = value;
        this.children = children;
    }

    public SimpleTreeNode(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public List<ITreeNode<K, V>> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<ITreeNode<K, V>> children) {
        this.children = children;
    }
}
