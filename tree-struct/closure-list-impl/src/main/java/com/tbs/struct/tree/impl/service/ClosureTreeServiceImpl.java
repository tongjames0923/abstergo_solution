package com.tbs.struct.tree.impl.service;

import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.sql.utils.TransactionUtil;
import com.tbs.struct.tree.api.persistence.ITreeStorable;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import com.tbs.struct.tree.impl.entity.TreeNodeEntity;
import com.tbs.struct.tree.impl.entity.TreeRelationEntity;
import com.tbs.struct.tree.impl.mapper.TreeNodeMapper;
import com.tbs.struct.tree.impl.mapper.TreeRelationMapper;
import com.tbs.struct.tree.impl.model.SimpleTreeStrorableModel;
import com.tbs.struct.tree.model.TreeNodeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ClosureTreeServiceImpl implements ITreeStore {
    @Resource
    private TreeNodeMapper treeNodeMapper;

    @Resource
    private TreeRelationMapper treeRelationMapper;

    private static ITreeStore instance;

    private static ITreeStore getInstance() {
        if (instance == null) {
            instance = SpringUtil.getBean(ITreeStore.class);
        }
        return instance;
    }

    @Override
    @Caching(evict = {@CacheEvict(cacheNames = "tree_node_cache", key = "#parentNodeId",
        condition = "#parentNodeId!=null&&#parentNodeId.length()>0"),
        @CacheEvict(cacheNames = "tree_node_cache", key = "#treeStorable.nodeId",
            condition = "#treeStorable!=null&&#treeStorable.nodeId!=null&&#treeStorable.nodeId.length()>0")})
    public void store(ITreeStorable treeStorable, String parentNodeId) {
        AssertUtil.notNull(treeStorable, "无法保存空节点");
        AssertUtil.hasText(treeStorable.getNodeId(), "无法保存空的节点id");
        AssertUtil.hasText(treeStorable.getTreeGroup(), "无法保存空的树组");

        TreeNodeEntity parentNode = getTreeNode(parentNodeId);
        List<TreeNodeEntity> oldNodes = treeNodeMapper.getNodeById(treeStorable.getNodeId());

        TransactionUtil.getInstance().executeTransaction(() -> {
            if (CollUtil.isEmpty(oldNodes)) {
                insertNewNode(treeStorable, parentNode);
            } else {
                updateExistingNode(treeStorable, parentNode, oldNodes);
            }
        });
    }

    /**
     * 获取树节点
     *
     * @param nodeId 节点ID
     * @return 树节点实体
     */
    private TreeNodeEntity getTreeNode(String nodeId) {
        if (StrUtil.isNotEmpty(nodeId)) {
            List<TreeNodeEntity> parentNodes = treeNodeMapper.getNodeById(nodeId);
            return CollUtil.first(parentNodes);
        }
        return null;
    }

    /**
     * 插入新节点
     *
     * @param treeStorable 树存储对象
     * @param parentNode   父节点
     */
    private void insertNewNode(ITreeStorable treeStorable, TreeNodeEntity parentNode) {
        TreeNodeEntity node = new TreeNodeEntity();
        node.setId(treeStorable.getNodeId());
        node.setGroupName(treeStorable.getTreeGroup());
        node.setCreateTime(new Date());

        //根节点插入
        if (parentNode == null) {
            log.info("插入根节点：{}", treeStorable.getNodeId());
            insertRootNode(node);
        } else {
            //子节点插入
            log.info("插入子节点：{}", treeStorable.getNodeId());
            insertChildNode(node, parentNode);
        }
    }

    /**
     * 插入根节点
     *
     * @param node 节点实体
     */
    private void insertRootNode(TreeNodeEntity node) {
        node.setParentId(null);
        treeNodeMapper.insert(node);

        TreeRelationEntity relationEntity = new TreeRelationEntity();
        relationEntity.setDepth(0);
        relationEntity.setAncestorId(node.getId());
        relationEntity.setDescendantId(node.getId());
        treeRelationMapper.insert(relationEntity);
    }

    /**
     * 插入子节点
     *
     * @param node       节点实体
     * @param parentNode 父节点
     */
    private void insertChildNode(TreeNodeEntity node, TreeNodeEntity parentNode) {
        node.setParentId(parentNode.getId());
        treeNodeMapper.insert(node);

        // 添加自己到自己的关系
        insertSelfRelation(node.getId());

        // 添加从父节点到当前节点的关系
        insertParentChildRelation(parentNode.getId(), node.getId());

        // 复制所有祖先节点到父节点的关系，并延伸到当前节点
        copyAncestorRelations(parentNode.getId(), node.getId());
    }

    /**
     * 添加自己到自己的关系
     *
     * @param nodeId 节点ID
     */
    private void insertSelfRelation(String nodeId) {
        TreeRelationEntity selfRelation = new TreeRelationEntity();
        selfRelation.setAncestorId(nodeId);
        selfRelation.setDescendantId(nodeId);
        selfRelation.setDepth(0);
        treeRelationMapper.insert(selfRelation);
    }

    /**
     * 添加父节点到子节点的关系
     *
     * @param parentId 父节点ID
     * @param childId  子节点ID
     */
    private void insertParentChildRelation(String parentId, String childId) {
        TreeRelationEntity parentRelation = new TreeRelationEntity();
        parentRelation.setAncestorId(parentId);
        parentRelation.setDescendantId(childId);
        parentRelation.setDepth(1);
        treeRelationMapper.insert(parentRelation);
    }

    /**
     * 复制祖先节点关系
     *
     * @param parentId 父节点ID
     * @param childId  子节点ID
     */
    private void copyAncestorRelations(String parentId, String childId) {
        List<TreeRelationEntity> ancestorRelations = treeRelationMapper.selectAncestorsByDescendantId(parentId);

        for (TreeRelationEntity relation : ancestorRelations) {
            if (!relation.getAncestorId().equals(parentId)) { // 排除父节点自身
                TreeRelationEntity newRelation = new TreeRelationEntity();
                newRelation.setAncestorId(relation.getAncestorId());
                newRelation.setDescendantId(childId);
                newRelation.setDepth(relation.getDepth() + 1);
                treeRelationMapper.insert(newRelation);
            }
        }
    }

    /**
     * 更新现有节点
     *
     * @param treeStorable 树存储对象
     * @param parentNode   父节点
     * @param oldNodes     旧节点列表
     */
    private void updateExistingNode(ITreeStorable treeStorable, TreeNodeEntity parentNode,
        List<TreeNodeEntity> oldNodes) {
        var oldNode = CollUtil.first(oldNodes);
        AssertUtil.isTrue(Objects.equals(treeStorable.getTreeGroup(), oldNode.getGroupName()), "树组不一致，无法更新");
        if (parentNode == null) {
            log.info("节点不支持升级到根节点");
            return;
        }
        if (Objects.equals(oldNode.getParentId(), parentNode.getId())) {
            log.info("节点关系未改变");
            return;
        }

        // 更新节点的父节点信息
        oldNode.setParentId(parentNode.getId());
        oldNode.setUpdateTime(new Date());
        treeNodeMapper.updateByPrimaryKey(oldNode);

        // 更新闭包表关系
        updateClosureRelations(treeStorable.getNodeId(), parentNode);
    }

    /**
     * 更新闭包表关系
     *
     * @param nodeId     节点ID
     * @param parentNode 父节点
     */
    private void updateClosureRelations(String nodeId, TreeNodeEntity parentNode) {
        // 1. 删除旧的闭包表关系
        // 1.1 获取节点的所有后代节点
        List<TreeRelationEntity> descendantRelations = treeRelationMapper.selectDescendantsByAncestorId(nodeId);

        deleteOldClosureRelations(nodeId);

        // 2. 添加新的闭包表关系
        insertNewClosureRelations(nodeId, parentNode);

        // 2.4 重新建立原有后代节点的关系
        rebuildDescendantRelations(nodeId, descendantRelations);
    }

    /**
     * 删除旧的闭包表关系
     *
     * @param nodeId 节点ID
     */
    private void deleteOldClosureRelations(String nodeId) {
        // 1.3 删除所有与该节点相关的路径关系
        treeRelationMapper.deleteByDescendantId(nodeId);
    }

    /**
     * 添加新的闭包表关系
     *
     * @param nodeId     节点ID
     * @param parentNode 父节点
     */
    private void insertNewClosureRelations(String nodeId, TreeNodeEntity parentNode) {
        // 2.1 添加自己到自己的关系
        insertSelfRelation(nodeId);

        // 2.2 添加从父节点到当前节点的关系
        insertParentChildRelation(parentNode.getId(), nodeId);

        // 2.3 添加父节点的所有祖先到当前节点的关系
        List<TreeRelationEntity> parentAncestorRelations =
            treeRelationMapper.selectAncestorsByDescendantId(parentNode.getId());
        for (TreeRelationEntity relation : parentAncestorRelations) {
            if (!relation.getAncestorId().equals(parentNode.getId())) { // 排除父节点自身
                TreeRelationEntity newRelation = new TreeRelationEntity();
                newRelation.setAncestorId(relation.getAncestorId());
                newRelation.setDescendantId(nodeId);
                newRelation.setDepth(relation.getDepth() + 1);
                treeRelationMapper.insert(newRelation);
            }
        }
    }

    /**
     * 重新建立后代节点的关系
     *
     * @param nodeId              节点ID
     * @param descendantRelations 后代关系列表
     */
    private void rebuildDescendantRelations(String nodeId, List<TreeRelationEntity> descendantRelations) {
        for (TreeRelationEntity descendantRelation : descendantRelations) {
            if (!descendantRelation.getDescendantId().equals(nodeId)) { // 排除节点自身
                // 计算新的深度：祖先到当前节点的深度 + 当前节点到后代节点的深度
                List<TreeRelationEntity> newAncestorRelations =
                    treeRelationMapper.selectAncestorsByDescendantId(nodeId);
                for (TreeRelationEntity newAncestorRelation : newAncestorRelations) {
                    TreeRelationEntity newRelation = new TreeRelationEntity();
                    newRelation.setAncestorId(newAncestorRelation.getAncestorId());
                    newRelation.setDescendantId(descendantRelation.getDescendantId());
                    newRelation.setDepth(newAncestorRelation.getDepth() + descendantRelation.getDepth());
                    treeRelationMapper.insert(newRelation);
                }
            }
        }
    }

    @Override
    public List<ITreeStorable> getChildren(String nodeId) {
        var coll = treeNodeMapper.getChildren(nodeId);
        return CollUtil.isEmpty(coll) ? Collections.EMPTY_LIST : coll.stream().map(treeNodeEntity -> {
            var model = new SimpleTreeStrorableModel();
            model.setNodeId(treeNodeEntity.getId());
            model.setTreeGroup(treeNodeEntity.getGroupName());
            return model;
        }).collect(Collectors.toList());
    }

    private ITreeStorable toTreeStorable(TreeNodeEntity treeNodeEntity) {
        var model = new SimpleTreeStrorableModel();
        model.setNodeId(treeNodeEntity.getId());
        model.setTreeGroup(treeNodeEntity.getGroupName());
        return model;
    }

    @Override
    public List<TreeNodeModel> getParentNodes(String nodeId) {
        var node = CollUtil.first(treeNodeMapper.getNodeById(nodeId));
        if (node == null) {
            return null;
        }

        return treeRelationMapper.selectAncestorsByDescendantId(nodeId).stream().map(relation -> {
            var model = new TreeNodeModel();
            model.setNodeId(relation.getAncestorId());
            model.setDepth(relation.getDepth());
            model.setTreeGroup(node.getGroupName());
            return model;
        }).sorted((o1, o2) -> o2.getDepth() - o1.getDepth()).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @CacheEvict(cacheNames = "tree_node_cache", key = "#nodeId")
    @Override
    public void delete(String nodeId) {
        if (StrUtil.isEmpty(nodeId)) {
            log.info("节点ID为空");
            return;
        }
        var node = CollUtil.first(treeNodeMapper.getNodeById(nodeId));
        if (node == null) {
            log.info("节点不存在");
            return;
        }

        // 通过闭包表获取所有后代节点ID（包括子树）
        List<TreeRelationEntity> descendantRelations = treeRelationMapper.selectDescendantsByAncestorId(nodeId);
        List<String> nodeIdsToDelete =
            descendantRelations.stream().map(TreeRelationEntity::getDescendantId).collect(Collectors.toList());
        // 添加当前节点本身
        nodeIdsToDelete.add(nodeId);

        // 批量删除节点
        for (String id : nodeIdsToDelete) {
            treeNodeMapper.deleteByPrimaryKey(id);
        }

        // 删除所有涉及这些节点的关系
        for (String id : nodeIdsToDelete) {
            // 删除涉及该节点的所有闭包表关系
            treeRelationMapper.deleteByDescendantId(id);
            treeRelationMapper.deleteByAncestorId(id);
        }
    }

    @Override
    public void batchDelete(List<String> nodeIds) {
        if (CollUtil.isEmpty(nodeIds)) {
            return;
        }
        nodeIds.forEach(nodeId -> {
            getInstance().delete(nodeId);
        });
    }

    @Cacheable(cacheNames = "tree_root_node", key = "#treeGroup")
    @ExpireOn(value = 2, timeUnit = TimeUnit.MINUTES)
    @Override
    public List<ITreeStorable> getRootNodes(String treeGroup) {
        AssertUtil.hasText(treeGroup, "无法查询空的树组");
        var coll = treeNodeMapper.getRootNodeByGroupName(treeGroup);
        return CollUtil.isEmpty(coll) ? Collections.EMPTY_LIST : coll.stream().map(treeNodeEntity -> {
            var model = new SimpleTreeStrorableModel();
            model.setNodeId(treeNodeEntity.getId());
            model.setTreeGroup(treeNodeEntity.getGroupName());
            return model;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> listTreeGroups() {
        return treeNodeMapper.listAllGroup();
    }

    @Override
    public boolean isExist(String nodeId) {
        AssertUtil.hasText(nodeId, "无法查询空的节点id");
        var coll = treeNodeMapper.getNodeById(nodeId);
        return CollUtil.isNotEmpty(coll);
    }
}
