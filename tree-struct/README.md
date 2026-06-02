# Tree-Struct 模块 - 树结构管理

## 模块概述
- **零配置启动**：自动配置数据库映射和实体扫描
- **标准化接口**：提供统一的树结构操作API
- **内存与持久化**：支持内存树结构和数据库持久化存储

### 核心特性

#### 开箱即用
- **闭包表算法**：基于闭包表实现高效的树关系存储
- **事务支持**：完整的数据库事务管理
- **缓存优化**：集成Spring Cache提供性能优化

#### 数据完整性
- **闭包表算法**：基于闭包表实现高效的树关系存储
- **多叉树支持**：支持任意多叉树结构
- **层级查询**：支持深度查询、叶子节点查询等
- **关系维护**：自动维护父子节点关系

#### 丰富功能
- **多叉树支持**：支持任意多叉树结构
- **接口抽象**：基于接口设计，易于扩展
- **多种实现**：提供内存树和闭包表两种实现方式
- **配置灵活**：支持自定义配置和扩展

#### 灵活扩展
- **接口抽象**：基于接口设计，易于扩展

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>tree-struct</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 基础配置

确保数据库表结构已创建：

```sql
-- 树节点表
CREATE TABLE tree_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    node_id VARCHAR(64) NOT NULL,
    group_name VARCHAR(64) NOT NULL,
    parent_id VARCHAR(64),
    create_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_node_group (group_name, node_id),
    INDEX idx_group_parent (group_name, parent_id)
);

-- 树闭包关系表
CREATE TABLE tree_closure (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ancestor_id VARCHAR(64) NOT NULL,
    descendant_id VARCHAR(64) NOT NULL,
    depth INT NOT NULL,
    group_name VARCHAR(64) NOT NULL,
    UNIQUE KEY uk_ancestor_descendant (ancestor_id, descendant_id),
    INDEX idx_ancestor (ancestor_id),
    INDEX idx_descendant (descendant_id),
    INDEX idx_depth (depth),
    INDEX idx_group (group_name)
);
```

### 3. 基本使用示例

#### 内存树结构使用

```java
// 创建多叉树
ITree<String, Object> tree = new MultiwayTree<>();

// 创建根节点
ITreeNode<String, Object> root = new SimpleTreeNode<>("root", "Root Node");
tree.setRoot(root);

// 添加子节点
ITreeNode<String, Object> child1 = new SimpleTreeNode<>("child1", "Child Node 1");
tree.insert("root", child1);

ITreeNode<String, Object> child2 = new SimpleTreeNode<>("child2", "Child Node 2");
tree.insert("root", child2);

// 添加孙子节点
ITreeNode<String, Object> grandchild = new SimpleTreeNode<>("grandchild", "Grandchild Node");
tree.insert("child1", grandchild);

// 遍历树
tree.traverse(node -> System.out.println(node.getKey() + ": " + node.getValue()));

// 查找节点
Optional<ITreeNode<String, Object>> found = tree.find("child1");
found.ifPresent(node -> System.out.println("Found: " + node.getValue()));

// 获取树高度
int height = tree.height();
System.out.println("Tree height: " + height);

// 获取叶子节点
List<ITreeNode<String, Object>> leaves = tree.getLeafNodes();
System.out.println("Leaf nodes: " + leaves.size());
```

#### 持久化树结构使用

```java
@Service
public class TreeService {
    
    @Resource
    private ITreeStore treeStore;
    
    // 存储树节点
    public void storeTreeNode(String nodeId, String treeGroup, String parentNodeId) {
        ITreeStorable node = new SimpleTreeStrorableModel();
        node.setNodeId(nodeId);
        node.setTreeGroup(treeGroup);
        treeStore.store(node, parentNodeId);
    }
    
    // 获取子节点
    public List<ITreeStorable> getChildren(String nodeId, String treeGroup) {
        return treeStore.getChildren(nodeId, treeGroup);
    }
    
    // 获取直接子节点
    public List<ITreeStorable> getDirectChildren(String nodeId, String treeGroup) {
        return treeStore.getDirectChildren(nodeId, treeGroup);
    }
    
    // 获取父节点链
    public List<TreeNodeModel> getParentNodes(String nodeId, String treeGroup) {
        return treeStore.getParentNodes(nodeId, treeGroup);
    }
    
    // 删除节点及其子树
    public void deleteNode(String nodeId, String treeGroup) {
        treeStore.delete(nodeId, treeGroup);
    }
}
```

## 快速开始

### 1. 树结构API层 (tree-api)

#### ITree<K, V> 接口
- **核心功能**：定义树结构的基本操作
- **方法包括**：插入、删除、查找、遍历、高度计算等
- **泛型支持**：支持任意键值类型

#### ITreeNode<K, V> 接口
- **节点定义**：定义树节点的基本结构
- **属性包括**：键、值、子节点列表
- **灵活扩展**：支持自定义节点实现

#### AbstractTree<K, V> 抽象类
- **模板模式**：提供树操作的基本实现框架
- **抽象方法**：insertNode、deleteNode等由子类实现
- **通用逻辑**：大小管理、根节点设置等

#### MultiwayTree<K, V> 实现类
- **多叉树实现**：支持任意数量的子节点
- **递归算法**：基于递归的插入、删除、查找操作
- **遍历支持**：深度优先遍历

#### TreeNodeModel 模型类
- **数据模型**：树节点的数据表示
- **持久化支持**：实现ITreeStorable接口
- **层级信息**：包含深度信息

### 2. 闭包表实现层 (closure-list-impl)

#### ClosureTreeServiceImpl 服务类
- **核心服务**：实现ITreeStore接口的闭包表服务
- **事务管理**：完整的数据库事务支持
- **缓存优化**：集成Spring Cache提升性能

#### TreeRelationEntity 实体类
- **关系存储**：存储节点间的祖先-后代关系
- **深度信息**：记录节点间的距离
- **分组支持**：支持多棵树并存

#### TreeNodeEntity 实体类
- **节点存储**：存储树节点的基本信息
- **父子关系**：记录直接父子关系（用于恢复）
- **时间戳**：创建和更新时间

#### TreeConfig 配置类
- **自动配置**：配置实体扫描和Mapper扫描
- **零配置**：开箱即用的配置

## 核心组件详解

### 1. 自定义树节点

``java
@Data
public class CustomTreeNode implements ITreeStorable {
    private String nodeId;
    private String treeGroup;
    private String customProperty;
    private Date createTime;
    
    // 实现ITreeStorable接口方法
    @Override
    public String getNodeId() {
        return nodeId;
    }
    
    @Override
    public String getTreeGroup() {
        return treeGroup;
    }
    
    // 自定义业务方法
    public boolean isLeaf() {
        // 自定义逻辑
        return true;
    }
}
```

### 2. 批量操作优化

``java
@Service
public class BatchTreeService {
    
    @Resource
    private ClosureTreeServiceImpl treeService;
    
    // 批量插入节点
    @Transactional
    public void batchInsertNodes(List<ITreeStorable> nodes, String parentNodeId) {
        for (ITreeStorable node : nodes) {
            treeService.store(node, parentNodeId);
        }
    }
    
    // 重建闭包表关系（数据修复）
    public void rebuildTreeClosure(String groupName) {
        treeService.buildTreeClosure(groupName);
    }
}
```

### 3. 缓存配置优化

``yaml
# application.yml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1小时

# 树节点缓存配置
cache:
  tree:
    node:
      ttl: 1800  # 30分钟
```

## 高级用法

### 1. 查询优化策略

``java
// 使用缓存提升查询性能
@Cacheable(cacheNames = "tree_node_cache", key = "#nodeId+#treeGroup")
public TreeNodeEntity getTreeNode(String nodeId, String treeGroup) {
    // 数据库查询逻辑
}

// 批量查询减少数据库交互
public List<ITreeStorable> getMultipleNodes(List<String> nodeIds, String treeGroup) {
    return nodeIds.stream()
        .map(nodeId -> treeStore.getChildren(nodeId, treeGroup))
        .flatMap(List::stream)
        .collect(Collectors.toList());
}
```

### 2. 数据库索引优化

``sql
-- 确保关键字段都有索引
CREATE INDEX idx_tree_node_group ON tree_node(group_name);
CREATE INDEX idx_tree_closure_ancestor ON tree_closure(ancestor_id);
CREATE INDEX idx_tree_closure_descendant ON tree_closure(descendant_id);
CREATE INDEX idx_tree_closure_group ON tree_closure(group_name);
```

## 性能优化

### 1. 日志配置

```
logging:
  level:
    com.tbs.struct.tree: DEBUG
```

### 2. 健康检查

``java
@Component
public class TreeHealthIndicator implements HealthIndicator {
    
    @Resource
    private ITreeStore treeStore;
    
    @Override
    public Health health() {
        try {
            List<String> groups = treeStore.listTreeGroups();
            return Health.up()
                .withDetail("treeGroups", groups.size())
                .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

## 监控与调试

### 1. 常见问题

#### 问题：节点关系不一致
**症状**：查询到的父子关系与预期不符
**解决方案**：
``java
// 重建闭包表关系
treeService.buildTreeClosure("your-tree-group");
```

#### 问题：性能下降
**症状**：树操作响应时间变长
**解决方案**：
- 检查数据库索引
- 优化缓存配置
- 分批处理大数据量

#### 问题：事务回滚失败
**症状**：部分操作未正确回滚
**解决方案**：
``java
@Transactional(rollbackFor = Exception.class)
public void safeTreeOperation() {
    // 业务逻辑
}
```

### 2. 调试技巧

``java
// 启用调试日志
@Slf4j
@Service
public class DebugTreeService {
    
    public void debugTreeOperation(String nodeId, String group) {
        log.debug("开始树操作 - 节点: {}, 分组: {}", nodeId, group);
        
        // 检查节点存在性
        boolean exists = treeStore.isExist(nodeId, group);
        log.debug("节点存在性: {}", exists);
        
        // 执行操作
        List<ITreeStorable> children = treeStore.getChildren(nodeId, group);
        log.debug("子节点数量: {}", children.size());
    }
}
```

## 故障排除

### 1. 设计原则

- **单一职责**：每个树分组负责单一业务领域
- **适度分层**：避免过深的树层级（建议不超过10层）
- **节点标识**：使用有意义的节点ID便于维护

### 2. 性能考虑

- **批量操作**：对于大量节点操作使用批量接口
- **缓存策略**：合理配置缓存时间和范围
- **索引优化**：确保关键查询字段都有索引

### 3. 数据一致性

- **事务边界**：明确事务边界，避免长事务
- **异常处理**：完善的异常处理和回滚机制
- **数据验证**：操作前验证数据的有效性

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

---

**最后更新**: 2026年6月
**版本**: 1.0-SNAPSHOT
