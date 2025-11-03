package com.tbs.struct.tree.impl.service;

import com.tbs.cache.CacheUtils;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ClosureTreeServiceImpl implements ITreeStore, IStartup {
    @Resource
    private TreeNodeMapper treeNodeMapper;

    @Resource
    private TreeRelationMapper treeRelationMapper;

    private static ITreeStore instance;

    private static ITreeStore getInstance() {
        return instance;
    }

    @Override
    @Caching(evict = {@CacheEvict(cacheNames = "tree_node_cache", key = "#parentNodeId",
        condition = "#parentNodeId!=null&&#parentNodeId.length()>0"),
        @CacheEvict(cacheNames = "tree_node_cache", key = "#treeStorable.nodeId",
            condition = "#treeStorable!=null&&#treeStorable.nodeId!=null&&#treeStorable.nodeId.length()>0")})
    @Transactional(rollbackFor = RuntimeException.class)
    public void store(ITreeStorable treeStorable, String parentNodeId) {

        AssertUtil.notNull(treeStorable, "无法保存空节点");
        AssertUtil.hasText(treeStorable.getNodeId(), "无法保存空的节点id");
        AssertUtil.hasText(treeStorable.getTreeGroup(), "无法保存空的树组");
        rebuildTreeClosureCheck();
        TreeNodeEntity parentNode = getTreeNode(parentNodeId);
        List<TreeNodeEntity> oldNodes = treeNodeMapper.getNodeById(treeStorable.getNodeId());
        if (CollUtil.isEmpty(oldNodes)) {
            insertNewNode(treeStorable, parentNode);
        } else {
            updateExistingNode(treeStorable, parentNode, oldNodes);
        }
    }

    /**
     * 获取树节点
     *
     * @param nodeId 节点ID
     * @return 树节点实体
     */
    private TreeNodeEntity getTreeNode(String nodeId) {
        if (StrUtil.isEmpty(nodeId)) {
            return null;
        }
        List<TreeNodeEntity> parentNodes = treeNodeMapper.getNodeById(nodeId);
        return CollUtil.first(parentNodes);
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
            if (relation.getAncestorId().equals(parentId)) {
                continue; // 排除父节点自身
            }
            TreeRelationEntity newRelation = new TreeRelationEntity();
            newRelation.setAncestorId(relation.getAncestorId());
            newRelation.setDescendantId(childId);
            newRelation.setDepth(relation.getDepth() + 1);
            treeRelationMapper.insert(newRelation);
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
        AssertUtil.isTrue(!Objects.equals(treeStorable.getTreeGroup(), oldNode.getGroupName()), "树组不一致，无法更新");
        if (parentNode == null) {
            log.info("节点不支持升级到根节点");
            return;
        }
        if (Objects.equals(oldNode.getParentId(), parentNode.getId())) {
            log.info("节点关系未改变");
            return;
        }
        var oldParentNodes = getInstance().getParentNodes(oldNode.getParentId());
        // 更新节点的父节点信息
        oldNode.setParentId(parentNode.getId());
        oldNode.setUpdateTime(new Date());
        treeNodeMapper.updateByPrimaryKey(oldNode);

        treeRelationMapper.deleteByDescendantId(oldNode.getId());

        var newParentNode = getInstance().getParentNodes(parentNode.getId());

        this.insertSelfRelation(oldNode.getId());
        insertParentChildRelation(parentNode.getId(), oldNode.getId());
        copyAncestorRelations(parentNode.getId(), oldNode.getId());

        var children = treeRelationMapper.selectDescendantsByAncestorId(oldNode.getId());
        for (var child : children) {
            if (child.getDescendantId().equals(oldNode.getId())) {
                continue;
            }
            //删除节点的子节点的无效父节点关系
            for (var oldParent : oldParentNodes) {
                treeRelationMapper.deleteByAncestorIdAndDescendantId(oldParent.getNodeId(), child.getDescendantId());
            }
            for (var newParent : newParentNode) {
                TreeRelationEntity newRelation = new TreeRelationEntity();
                newRelation.setAncestorId(newParent.getNodeId());
                newRelation.setDescendantId(child.getDescendantId());
                newRelation.setDepth(newParent.getDepth() + child.getDepth() + 1);
                treeRelationMapper.insert(newRelation);
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

    @Override
    public List<TreeNodeModel> getParentNodes(String nodeId) {
        var node = CollUtil.first(treeNodeMapper.getNodeById(nodeId));
        if (node == null) {
            return null;
        }
        rebuildTreeClosureCheck();
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
        rebuildTreeClosureCheck();
        // 通过闭包表获取所有后代节点ID（包括子树）
        List<TreeRelationEntity> descendantRelations = treeRelationMapper.selectDescendantsByAncestorId(nodeId);
        List<String> nodeIdsToDelete =
            descendantRelations.stream().map(TreeRelationEntity::getDescendantId).collect(Collectors.toList());
        // 添加当前节点本身
        nodeIdsToDelete.add(nodeId);

        // 批量删除节点
        treeNodeMapper.deleteByIdList(nodeIdsToDelete);

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

    /**
     * 重建树结构闭包表关系表
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public void buildTreeClosure() {
        CacheUtils.getInstance().cacheableSet(REBUILD_TREE_CLOSURE_LOCK, true);
        try {
            // 删除所有关系
            treeRelationMapper.deleteAllRelations();
            //查询所有的节点
            List<TreeNodeEntity> allNodes = treeNodeMapper.selectAll();
            //根据所有节点的entity的父节点ID来插入关系，符合闭包表关系

            // 创建一个Map来存储节点ID到TreeNodeEntity的映射，便于快速查找
            Map<String, TreeNodeEntity> nodeMap =
                allNodes.stream().collect(Collectors.toMap(TreeNodeEntity::getId, node -> node));

            // 先为所有节点建立自引用关系
            for (TreeNodeEntity node : allNodes) {
                TreeRelationEntity selfRelation = new TreeRelationEntity();
                selfRelation.setAncestorId(node.getId());
                selfRelation.setDescendantId(node.getId());
                selfRelation.setDepth(0);
                treeRelationMapper.insert(selfRelation);
            }

            // 按层级排序处理节点，确保父节点在子节点之前处理
            // 这样可以保证当处理子节点时，其所有祖先节点的关系已经建立完成
            List<TreeNodeEntity> sortedNodes = new ArrayList<>(allNodes);
            sortedNodes.sort((a, b) -> {
                int levelA = getNodeLevel(a, nodeMap);
                int levelB = getNodeLevel(b, nodeMap);
                return levelA - levelB;
            });

            // 再处理父子关系以及祖先关系
            for (TreeNodeEntity node : sortedNodes) {
                // 如果是根节点，跳过处理父子关系
                if (node.getParentId() == null || node.getParentId().isEmpty()) {
                    continue;
                }

                // 添加直接父子关系（深度为1）
                TreeRelationEntity parentRelation = new TreeRelationEntity();
                parentRelation.setAncestorId(node.getParentId());
                parentRelation.setDescendantId(node.getId());
                parentRelation.setDepth(1);
                treeRelationMapper.insert(parentRelation);

                // 查询父节点的所有祖先关系
                List<TreeRelationEntity> ancestorRelations =
                    treeRelationMapper.selectAncestorsByDescendantId(node.getParentId());

                // 为当前节点添加与所有祖先节点的关系
                for (TreeRelationEntity ancestorRelation : ancestorRelations) {
                    // 跳过父节点自身的自引用关系（已经在上面处理过了）
                    if (ancestorRelation.getAncestorId().equals(node.getParentId()) &&
                        ancestorRelation.getDepth() == 0) {
                        continue;
                    }

                    TreeRelationEntity newRelation = new TreeRelationEntity();
                    newRelation.setAncestorId(ancestorRelation.getAncestorId());
                    newRelation.setDescendantId(node.getId());
                    newRelation.setDepth(ancestorRelation.getDepth() + 1);
                    treeRelationMapper.insert(newRelation);
                }
            }

        } finally {
            CacheUtils.getInstance().cacheableEvict(REBUILD_TREE_CLOSURE_LOCK);
        }

    }

    /**
     * 计算节点在树中的层级（深度）
     *
     * @param node    节点
     * @param nodeMap 节点映射表
     * @return 层级数，根节点为0
     */
    private int getNodeLevel(TreeNodeEntity node, Map<String, TreeNodeEntity> nodeMap) {
        int level = 0;
        TreeNodeEntity current = node;
        while (current.getParentId() != null && !current.getParentId().isEmpty()) {
            level++;
            current = nodeMap.get(current.getParentId());
            // 防止循环引用导致的无限循环
            if (current == null) {
                break;
            }
        }
        return level;
    }

    private static final String REBUILD_TREE_CLOSURE_LOCK = "rebuildTreeLock";

    private static void rebuildTreeClosureCheck() {
        var val = CacheUtils.getInstance().cacheableGet(REBUILD_TREE_CLOSURE_LOCK);
        if (val == null) {
            return;
        }
        throw new RuntimeException("正在重建树结构闭包表关系，请稍后再试");
    }

    @Override
    public void startUp() throws RuntimeException {
        if (instance == null) {
            instance = SpringUtil.getBean(ITreeStore.class);
        }
    }
}
