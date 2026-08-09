package com.tbs.struct.tree.closure.impl.service;

import com.tbs.cache.CacheUtils;
import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.CollWrapper;
import com.tbs.common.util.base.StrUtil;
import com.tbs.struct.tree.api.persistence.ITreeStorable;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import com.tbs.struct.tree.closure.impl.entity.TreeNodeEntity;
import com.tbs.struct.tree.closure.impl.entity.TreeRelationEntity;
import com.tbs.struct.tree.closure.impl.mapper.TreeNodeMapper;
import com.tbs.struct.tree.closure.impl.mapper.TreeRelationMapper;
import com.tbs.struct.tree.closure.impl.model.SimpleTreeStrorableModel;
import com.tbs.struct.tree.model.TreeNodeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 树结构存储实现
 *
 * @author tongj
 */
@Slf4j
public class ClosureTreeServiceImpl implements ITreeStore<Long>, IStartup {
    @Resource
    private TreeNodeMapper treeNodeMapper;

    @Resource
    private TreeRelationMapper treeRelationMapper;

    @Resource
    private CacheManager cacheManager;

    private static ClosureTreeServiceImpl instance;

    private static ClosureTreeServiceImpl getInstance() {
        return instance;
    }

    private static TreeRelationEntity createRelation(String ancestorId, String descendantId, int depth,
        String groupName) {
        TreeRelationEntity relationEntity = new TreeRelationEntity();
        relationEntity.setAncestorId(ancestorId);
        relationEntity.setDescendantId(descendantId);
        relationEntity.setDepth(depth);
        relationEntity.setGroupName(groupName);
        return relationEntity;
    }

    private static TreeRelationEntity createSelfRelation(String nodeId, String groupName) {
        return createRelation(nodeId, nodeId, 0, groupName);
    }

    public Long getTreeNodeId(String nodeId, String treeGroup) {
        return treeNodeMapper.getNodeByNodeIdAndTreeGroup(nodeId, treeGroup).stream().findFirst()
            .map(TreeNodeEntity::getId).orElse(null);
    }

    /**
     * 获取树节点
     *
     * @param nodeId    节点ID
     * @param treeGroup 树组
     * @return 树节点
     */

    @Cacheable(cacheNames = "tree_node_entity", key = "#treeGroup+':'+#nodeId")
    public TreeNodeEntity getTreeNode(String nodeId, String treeGroup) {
        Long id = getInstance().getTreeNodeId(nodeId, treeGroup);
        if (id == null) {
            return null;
        }
        return treeNodeMapper.getNodeById(id);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void store(ITreeStorable treeStorable, String parentNodeId) {

        AssertUtil.notNull(treeStorable, "无法保存空节点");
        AssertUtil.hasText(treeStorable.getNodeId(), "无法保存空的节点id");
        AssertUtil.hasText(treeStorable.getTreeGroup(), "无法保存空的树组");
        rebuildTreeClosureCheck();
        TreeNodeEntity parentNode = getInstance().getTreeNode(parentNodeId, treeStorable.getTreeGroup());
        var oldNode = getInstance().getTreeNode(treeStorable.getNodeId(), treeStorable.getTreeGroup());
        if (oldNode == null) {
            insertNewNode(treeStorable, parentNode);
        } else {
            updateExistingNode(treeStorable, parentNode, oldNode);
        }
    }

    /**
     * 插入新节点
     *
     * @param treeStorable 树存储对象
     * @param parentNode   父节点
     */
    private void insertNewNode(ITreeStorable treeStorable, TreeNodeEntity parentNode) {
        TreeNodeEntity node = new TreeNodeEntity();
        node.setNodeId(treeStorable.getNodeId());
        node.setGroupName(treeStorable.getTreeGroup());
        node.setEntityId(treeStorable.getEntityId());
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

        treeRelationMapper.insert(createSelfRelation(node.getNodeId(), node.getGroupName()));
    }

    /**
     * 插入子节点
     *
     * @param node       节点实体
     * @param parentNode 父节点
     */
    private void insertChildNode(TreeNodeEntity node, TreeNodeEntity parentNode) {
        node.setParentId(parentNode.getNodeId());
        treeNodeMapper.insert(node);

        // 添加自己到自己的关系
        insertSelfRelation(node.getNodeId(), node.getGroupName());

        // 添加从父节点到当前节点的关系
        insertParentChildRelation(parentNode.getNodeId(), node.getNodeId(), parentNode.getGroupName());

        // 复制所有祖先节点到父节点的关系，并延伸到当前节点
        copyAncestorRelations(parentNode.getNodeId(), node.getNodeId(), parentNode.getGroupName());
    }

    /**
     * 添加自己到自己的关系
     *
     * @param nodeId 节点ID
     */
    private void insertSelfRelation(String nodeId, String group) {
        treeRelationMapper.insert(createSelfRelation(nodeId, group));
    }

    /**
     * 添加父节点到子节点的关系
     *
     * @param parentId 父节点ID
     * @param childId  子节点ID
     */
    private void insertParentChildRelation(String parentId, String childId, String groupName) {
        treeRelationMapper.insert(createRelation(parentId, childId, 1, groupName));
    }

    /**
     * 复制祖先节点关系
     *
     * @param parentId 父节点ID
     * @param childId  子节点ID
     */
    private void copyAncestorRelations(String parentId, String childId, String groupName) {
        List<TreeRelationEntity> ancestorRelations =
            treeRelationMapper.selectAncestorsByDescendantId(parentId, groupName);

        for (TreeRelationEntity relation : ancestorRelations) {
            if (relation.getAncestorId().equals(parentId)) {
                continue; // 排除父节点自身
            }
            treeRelationMapper.insert(
                createRelation(relation.getAncestorId(), childId, relation.getDepth() + 1, groupName));
        }
    }

    /**
     * 更新现有节点
     *
     * @param treeStorable 树存储对象
     * @param parentNode   父节点
     * @param oldNodes     旧节点列表
     */
    private void updateExistingNode(ITreeStorable treeStorable, TreeNodeEntity parentNode, TreeNodeEntity oldNode) {

        AssertUtil.isTrue(!Objects.equals(treeStorable.getTreeGroup(), oldNode.getGroupName()), "树组不一致，无法更新");
        if (parentNode == null) {
            log.info("节点不支持升级到根节点");
            return;
        }
        if (Objects.equals(oldNode.getParentId(), parentNode.getNodeId())) {
            log.info("节点关系未改变");
            // 即使关系未改变，也需要更新entityId
            if (treeStorable.getEntityId() != null &&
                !Objects.equals(treeStorable.getEntityId(), oldNode.getEntityId())) {
                oldNode.setEntityId(treeStorable.getEntityId());
                oldNode.setUpdateTime(new Date());
                treeNodeMapper.updateByPrimaryKey(oldNode);
            }
            return;
        }
        var oldParentNodes = getInstance().getParentNodes(oldNode.getParentId(), treeStorable.getTreeGroup());
        // 更新节点的父节点信息和entityId
        oldNode.setParentId(parentNode.getNodeId());
        if (treeStorable.getEntityId() != null) {
            oldNode.setEntityId(treeStorable.getEntityId());
        }
        oldNode.setUpdateTime(new Date());
        treeNodeMapper.updateByPrimaryKey(oldNode);

        treeRelationMapper.deleteByDescendantId(oldNode.getNodeId(), oldNode.getGroupName());

        var newParentNode = getInstance().getParentNodes(parentNode.getNodeId(), treeStorable.getTreeGroup());

        this.insertSelfRelation(oldNode.getNodeId(), oldNode.getGroupName());
        insertParentChildRelation(parentNode.getNodeId(), oldNode.getNodeId(), parentNode.getGroupName());
        copyAncestorRelations(parentNode.getNodeId(), oldNode.getNodeId(), parentNode.getGroupName());

        var children = treeRelationMapper.selectDescendantsByAncestorId(oldNode.getNodeId(), oldNode.getGroupName());
        for (var child : children) {
            if (child.getDescendantId().equals(oldNode.getNodeId())) {
                continue;
            }
            //删除节点的子节点的无效父节点关系
            for (var oldParent : oldParentNodes) {
                treeRelationMapper.deleteByAncestorIdAndDescendantId(oldParent.getNodeId(), child.getDescendantId(),
                    oldParent.getTreeGroup());
            }
            for (var newParent : newParentNode) {
                treeRelationMapper.insert(createRelation(newParent.getNodeId(), child.getDescendantId(),
                    newParent.getDepth() + child.getDepth() + 1, newParent.getTreeGroup()));
            }

        }

    }

    @Override
    public List<ITreeStorable> getDirectChildren(String nodeId, String group) {
        if (StrUtil.isEmpty(nodeId) || StrUtil.isEmpty(group)) {
            return Collections.EMPTY_LIST;
        }
        var relation = new TreeRelationEntity();
        relation.setAncestorId(nodeId);
        relation.setDepth(1);
        relation.setGroupName(group);
        return treeRelationMapper.select(relation).stream().map(res -> {
            var model = new SimpleTreeStrorableModel();
            model.setTreeGroup(res.getGroupName());
            model.setNodeId(res.getDescendantId());
            // 获取节点实体以填充entityId
            var treeNode = getInstance().getTreeNode(res.getDescendantId(), res.getGroupName());
            if (treeNode != null) {
                model.setEntityId(treeNode.getEntityId());
            }
            return model;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TreeNodeModel> getParentNodes(String nodeId, String group) {
        var node = getInstance().getTreeNode(nodeId, group);
        if (node == null) {
            return List.of();
        }
        rebuildTreeClosureCheck();
        var val = treeRelationMapper.selectAncestorsByDescendantId(nodeId, group).stream().map(relation -> {
            var model = new TreeNodeModel();
            model.setNodeId(relation.getAncestorId());
            model.setDepth(relation.getDepth());
            model.setTreeGroup(node.getGroupName());
            return model;
        }).sorted((o1, o2) -> o2.getDepth() - o1.getDepth()).collect(Collectors.toList());

        val.forEach(model -> {
            var treeNode = getInstance().getTreeNode(model.getNodeId(), model.getTreeGroup());
            if (treeNode != null) {
                model.setEntityId(treeNode.getEntityId());
            }
        });

        return val;
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    @CacheEvict(cacheNames = "tree_node_entity", key = "#treeGroup+':'+#nodeId")
    public void delete(String nodeId, String group) {
        if (StrUtil.isEmpty(nodeId)) {
            log.info("节点ID为空");
            return;
        }
        var node = getInstance().getTreeNode(nodeId, group);
        if (node == null) {
            log.info("节点不存在");
            return;
        }
        rebuildTreeClosureCheck();
        // 通过闭包表获取所有后代节点ID（包括子树）
        List<TreeRelationEntity> descendantRelations = treeRelationMapper.selectDescendantsByAncestorId(nodeId, group);
        var nodeIdsToDelete = descendantRelations.stream().map((val) -> {
            return getInstance().getTreeNode(val.getDescendantId(), group);
        }).collect(Collectors.toList());
        // 添加当前节点本身
        nodeIdsToDelete.add(node);

        // 批量删除节点
        treeNodeMapper.deleteByIdList(nodeIdsToDelete.stream().map(TreeNodeEntity::getId).collect(Collectors.toList()));

        // 删除所有涉及这些节点的关系
        for (var entity : nodeIdsToDelete) {
            // 删除涉及该节点的所有闭包表关系
            treeRelationMapper.deleteByDescendantId(entity.getNodeId(), group);
            treeRelationMapper.deleteByAncestorId(entity.getNodeId(), group);
        }
    }

    @Override
    public List<ITreeStorable> getRootNodes(String treeGroup) {
        AssertUtil.hasText(treeGroup, "无法查询空的树组");
        var coll = treeNodeMapper.getRootNodeByGroupName(treeGroup);
        return CollUtil.isEmpty(coll) ? Collections.EMPTY_LIST : coll.stream().map(treeNodeEntity -> {
            var model = new SimpleTreeStrorableModel();
            model.setNodeId(treeNodeEntity.getNodeId());
            model.setTreeGroup(treeNodeEntity.getGroupName());
            model.setEntityId(treeNodeEntity.getEntityId());
            return model;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> listTreeGroups() {
        return treeNodeMapper.listAllGroup();
    }

    @Override
    public boolean isExist(String nodeId, String group) {
        AssertUtil.hasText(nodeId, "无法查询空的节点id");
        return getInstance().getTreeNode(nodeId, group) != null;
    }

    /**
     * 重建树结构闭包表关系表
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    @CacheEvict(cacheNames = "tree_node_entity", allEntries = true)
    public void tryRebuildStructure(String groupName) {
        rebuildTreeClosureCheck();
        // 先抢占JVM本地锁，缩小检查与加锁之间的竞态窗口；抢锁失败（坏条件）时抛异常
        AssertUtil.toAssert(!REBUILD_LOCAL_LOCK.tryLock()).message("正在重建树结构闭包表关系，请稍后再试")
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        boolean lockHandedOver = false;
        try {
            CacheUtils.getInstance().cacheableSet(REBUILD_TREE_CLOSURE_LOCK, true);
            // 仅删除目标树组的关系，避免误删其他树组的闭包关系；组为空时才全量重建
            if (StrUtil.isEmpty(groupName)) {
                treeRelationMapper.deleteAllRelations();
            } else {
                treeRelationMapper.deleteRelationsByGroup(groupName);
            }
            //查询节点
            TreeNodeEntity search = new TreeNodeEntity();
            search.setGroupName(StrUtil.isEmpty(groupName) ? null : groupName);
            List<TreeNodeEntity> allNodes = treeNodeMapper.select(search);
            //根据所有节点的entity的父节点ID来插入关系，符合闭包表关系

            if (CollUtil.isEmpty(allNodes)) {
                return;
            }

            // 创建一个Map来存储节点ID到TreeNodeEntity的映射，便于快速查找
            Map<String, TreeNodeEntity> nodeMap =
                allNodes.stream().collect(Collectors.toMap(TreeNodeEntity::getNodeId, node -> node, (a, b) -> {
                    throw new FormattedRuntimeException("树组[{}]中存在重复的节点ID：{}", a.getGroupName(),
                        a.getNodeId());
                }));

            List<TreeRelationEntity> relations = new ArrayList<>(allNodes.size() * 2);
            for (var node : allNodes) {
                relations.addAll(traceRelations(nodeMap, node.getNodeId()));
            }
            CollUtil.of(relations).partition(50).map(CollWrapper::toList).forEach(treeRelationMapper::insertList);

            // 事务提交/回滚完成后再释放锁，避免提交阶段其他写操作趁虚而入
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        releaseRebuildLock();
                    }
                });
                lockHandedOver = true;
            }
        } finally {
            if (!lockHandedOver) {
                releaseRebuildLock();
            }
        }

    }

    List<TreeRelationEntity> traceRelations(Map<String, TreeNodeEntity> nodeMap, String nodeId) {
        var relations = new ArrayList<TreeRelationEntity>();
        TreeNodeEntity current = nodeMap.get(nodeId);
        if (current == null) {
            return relations;
        }

        relations.add(createSelfRelation(current.getNodeId(), current.getGroupName()));
        // 记录已上溯过的节点，防止父指针成环导致死循环
        Set<String> visited = new HashSet<>();
        visited.add(current.getNodeId());
        var temp = current;
        int dep = 1;
        while (StrUtil.isNotEmpty(temp.getParentId())) {
            String parentId = temp.getParentId();
            temp = nodeMap.get(parentId);
            AssertUtil.notNull(temp, "无法找到节点：{}", parentId);
            // visited.add 返回false表示节点已上溯过，即存在环（坏条件），此时抛异常
            AssertUtil.toAssert(!visited.add(temp.getNodeId()))
                .message("树结构存在环，无法重建闭包关系，节点：{}", temp.getNodeId())
                .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
            relations.add(createRelation(temp.getNodeId(), current.getNodeId(), dep++, temp.getGroupName()));
        }
        return relations;
    }



    private static final String REBUILD_TREE_CLOSURE_LOCK = "rebuildTreeLock";

    /**
     * JVM本地重建锁，防止同一进程内并发线程同时通过锁检查进入重建流程
     */
    private static final ReentrantLock REBUILD_LOCAL_LOCK = new ReentrantLock();

    private static void rebuildTreeClosureCheck() {
        var val = CacheUtils.getInstance().cacheableGet(REBUILD_TREE_CLOSURE_LOCK);
        AssertUtil.toAssert(val != null).message("正在重建树结构闭包表关系，请稍后再试")
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
    }

    /**
     * 释放重建锁（缓存锁 + JVM本地锁）
     */
    private static void releaseRebuildLock() {
        CacheUtils.getInstance().cacheableEvict(REBUILD_TREE_CLOSURE_LOCK);
        REBUILD_LOCAL_LOCK.unlock();
    }

    @Override
    public ITreeStorable getNode(String nodeId, String group) {
        var node = ClosureTreeServiceImpl.getInstance().getTreeNode(nodeId, group);
        if (node == null) {
            return null;
        }
        var model = new TreeNodeModel();
        model.setNodeId(node.getNodeId());
        model.setTreeGroup(node.getGroupName());
        model.setEntityId(node.getEntityId());
        model.setDepth(-1);
        return model;
    }

    @Override
    public List<ITreeStorable> getNodesByEntityId(Long id) {
        return treeNodeMapper.getNodesByEntityId(id).stream()
            .map(node -> new SimpleTreeStrorableModel(node.getGroupName(), node.getNodeId(), node.getEntityId()))
            .collect(Collectors.toList());
    }

    @Override
    public void startUp() throws RuntimeException {
        if (instance == null) {
            instance = this;
        }
    }
}
