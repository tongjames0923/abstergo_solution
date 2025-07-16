package com.tbs.common.tree.impl;

import com.tbs.common.tree.AbstractTree;
import com.tbs.common.tree.ITreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 多叉树
 *
 * @author abstergo
 * @param <K>
 * @param <V>
 */
public class MultiwayTree<K, V> extends AbstractTree<K, V> {
    @Override
    protected void insertNode(ITreeNode<K, V> currentNode, K parentKey, ITreeNode<K, V> newNode) {
        if (currentNode.getKey().equals(parentKey)) {
            List<ITreeNode<K, V>> children = currentNode.getChildren();
            if (children == null) {
                children = new ArrayList<>();
                currentNode.setChildren(children);
            }
            children.add(newNode);
            size++;
            return;
        }

        List<ITreeNode<K, V>> children = currentNode.getChildren();
        if (children != null) {
            for (ITreeNode<K, V> child : children) {
                insertNode(child, parentKey, newNode);
            }
        }
    }

    @Override
    protected boolean deleteNode(ITreeNode<K, V> parent, ITreeNode<K, V> currentNode, K key) {
        if (currentNode.getKey().equals(key)) {
            if (parent == null) {
                root = null; // 删除根节点
            } else {
                List<ITreeNode<K, V>> siblings = parent.getChildren();
                if (siblings != null) {
                    siblings.remove(currentNode);
                }
            }
            return true;
        }

        List<ITreeNode<K, V>> children = currentNode.getChildren();
        if (children != null) {
            for (ITreeNode<K, V> child : children) {
                if (deleteNode(currentNode, child, key)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void traverse(Consumer<ITreeNode<K, V>> visitor) {
        if (root != null) {
            traverseNode(root, visitor);
        }
    }

    private void traverseNode(ITreeNode<K, V> node, Consumer<ITreeNode<K, V>> visitor) {
        visitor.accept(node);
        List<ITreeNode<K, V>> children = node.getChildren();
        if (children != null) {
            for (ITreeNode<K, V> child : children) {
                traverseNode(child, visitor);
            }
        }
    }

    @Override
    public Optional<ITreeNode<K, V>> find(K key) {
        if (root == null) {
            return Optional.empty();
        }
        return findNode(root, key);
    }

    private Optional<ITreeNode<K, V>> findNode(ITreeNode<K, V> node, K key) {
        if (node.getKey().equals(key)) {
            return Optional.of(node);
        }

        List<ITreeNode<K, V>> children = node.getChildren();
        if (children != null) {
            for (ITreeNode<K, V> child : children) {
                Optional<ITreeNode<K, V>> result = findNode(child, key);
                if (result.isPresent()) {
                    return result;
                }
            }
        }
        return Optional.empty();
    }
}
