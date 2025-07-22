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
            setSize(1);
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
                // 删除根节点
                setRoot(null);
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
        if (getRoot() != null) {
            traverseNode(getRoot(), visitor);
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
        if (getRoot() == null) {
            return Optional.empty();
        }
        return findNode(getRoot(), key);
    }

    @Override
    public int height() {
        return calculateHeight(getRoot());
    }

    private int calculateHeight(ITreeNode<K, V> node) {
        if (node == null) {
            return 0;
        }

        List<ITreeNode<K, V>> children = node.getChildren();
        if (children == null || children.isEmpty()) {
            return 1;
        }

        int maxChildHeight = 0;
        for (ITreeNode<K, V> child : children) {
            int childHeight = calculateHeight(child);
            if (childHeight > maxChildHeight) {
                maxChildHeight = childHeight;
            }
        }

        return maxChildHeight + 1;
    }

    @Override
    public List<ITreeNode<K, V>> getLeafNodes() {
        List<ITreeNode<K, V>> leafNodes = new ArrayList<>();
        if (getRoot() != null) {
            collectLeafNodes(getRoot(), leafNodes);
        }
        return leafNodes;
    }

    private void collectLeafNodes(ITreeNode<K, V> node, List<ITreeNode<K, V>> leafNodes) {
        List<ITreeNode<K, V>> children = node.getChildren();
        if (children == null || children.isEmpty()) {
            leafNodes.add(node);
        } else {
            for (ITreeNode<K, V> child : children) {
                collectLeafNodes(child, leafNodes);
            }
        }
    }

    @Override
    public List<ITreeNode<K, V>> getNodesByDepth(int depth) {
        List<ITreeNode<K, V>> nodes = new ArrayList<>(4);
        if (getRoot() != null && depth > 0) {
            collectNodesByDepth(getRoot(), depth, 1, nodes);
        }
        return nodes;
    }

    private void collectNodesByDepth(ITreeNode<K, V> node, int targetDepth, int currentDepth,
        List<ITreeNode<K, V>> nodes) {
        if (currentDepth == targetDepth) {
            nodes.add(node);
            return;
        }

        List<ITreeNode<K, V>> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            for (ITreeNode<K, V> child : children) {
                collectNodesByDepth(child, targetDepth, currentDepth + 1, nodes);
            }
        }
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
