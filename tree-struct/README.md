# Tree-Struct 模块 - 树结构管理

## 模块概述

Tree-Struct 模块（`com.tbs:tree-struct`）提供两套互补的树结构能力：

| 能力 | 所在子模块 | 说明 |
|------|-----------|------|
| **内存多叉树** | `tree-api`（`com.tbs.struct.tree.model`） | `ITree` / `ITreeNode` 接口 + `MultiwayTree` 递归实现，纯内存、无持久化 |
| **闭包表持久化树** | `closure-list-impl`（`com.tbs.struct.tree.closure`） | `ITreeStore` 的 MySQL 实现，支持多树分组、子树删除、关系重建 |

设计要点：

- **闭包表（Closure Table）**：以 `tree_closure` 冗余存储"祖先-后代"全路径关系（含自身 depth=0），查询子树/祖先链均为单表索引查询；`tree_node` 额外保留 `parent_id` 作为**关系备份**（闭包损坏时可据此重建）；
- **树分组（treeGroup）**：同一套表可并存多棵逻辑树，所有操作均需携带树组名；
- **节点与业务解耦**：节点携带 `entityId` 指向业务实体，树结构模块不感知业务含义。

| 子模块 | artifactId | 依赖 |
|--------|------------|------|
| `tree-api` | `tree-api` | （继承父 POM：`cache-api`、`async`、`common`） |
| `closure-list-impl` | `closure-list-impl` | `tree-api`、`sql`、`xxl-job` |

父 POM `com.tbs:tree-struct` 为 `packaging=pom` 聚合模块——**不可作为依赖直接引入**（旧文档的 `com.tbs:tree-struct` 依赖写法无效）。

## 模块结构

```
tree-struct/
├── tree-api/                          # 契约与内存实现（9 个源文件）
│   └── com.tbs.struct.tree/
│       ├── api/                       # ITree / ITreeNode / AbstractTree
│       │   └── persistence/           # ITreeStorable / ITreeStore<ID>
│       │       └── impl/              # NoImplTreeStore（构造即抛异常）
│       └── model/                     # MultiwayTree / SimpleTreeNode / TreeNodeModel
└── closure-list-impl/                 # 闭包表实现（10 个源文件）
    └── com.tbs.struct.tree.closure/
        ├── annotation/                # @UseClosureTableTree
        └── impl/
            ├── config/                # ClosureTableAutoRegister / TreeConfig
            ├── entity/                # TreeNodeEntity / TreeRelationEntity
            ├── mapper/                # TreeNodeMapper / TreeRelationMapper
            ├── model/                 # SimpleTreeStrorableModel
            └── service/               # ClosureTreeServiceImpl / CacheNamesConstants
```

## 启用方式（闭包表实现）

```java
@SpringBootApplication
@UseClosureTableTree   // = @EnableCommonPack + @Import(ClosureTableAutoRegister)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

- `ClosureTableAutoRegister`：扫描 `com.tbs.struct.tree.closure` 包，注册 `Feature.CLOSURE_TABLE`；
- `TreeConfig`：`@MapperScan("com.tbs.struct.tree.closure.impl.mapper")`（实体由 `jkit.sql.auto.packages` 扫描）；
- **表结构由 jkit-sql-auto（`mode: update`）自动创建/更新**，无需手工执行建表 SQL；
- 模块**无 `spring.factories`**，无 i18n 资源文件。

> sql 引擎与 `SqlDialect` 由 `sql-impl` + 方言模块（`sql-mysql-impl` 等）依赖经 Spring Boot 自动装配提供；`@UseClosureTableTree` 已不再内含 `@EnableTbsSql`，也无需 `@UseMysql`。

## 数据模型

### tree_node（TreeNodeEntity）

| 字段 | 列 | 说明 |
|------|-----|------|
| `id` | `id` | 主键，自增 |
| `nodeId` | `node_id`(64) | 节点 ID（业务侧唯一标识） |
| `groupName` | `group_name`(64) | 树组名（作为树的 ID） |
| `parentId` | `parent_id`(64) | 父节点 ID，**null 表示根节点**；作为关系备份 |
| `entityId` | `entity_id` | 关联的业务实体 ID |
| `createTime` / `updateTime` | `create_time` / `update_time` | 审计时间 |

唯一约束：`(group_name, node_id)`；索引：`(group_name, parent_id)`、`(entity_id)`。

### tree_closure（TreeRelationEntity）

| 字段 | 列 | 说明 |
|------|-----|------|
| `id` | `id` | 主键，自增 |
| `ancestorId` | `ancestor_id`(64, 非空) | 祖先节点 ID |
| `descendantId` | `descendant_id`(64, 非空) | 后代节点 ID |
| `depth` | `depth`(非空) | 距离：0=自身，1=父子，2=祖孙… |
| `groupName` | `group_name`(64, 非空) | 树组名 |

唯一约束：`(ancestor_id, descendant_id)`；索引：`ancestor_id`、`descendant_id`、`depth`、`group_name`。

## 一、内存多叉树（tree-api）

### 接口定义

```java
public interface ITree<K, V> {
    ITreeNode<K, V> getRoot();
    void setRoot(ITreeNode<K, V> root);
    int size();
    boolean isEmpty();
    void traverse(Consumer<ITreeNode<K, V>> visitor);  // 深度优先遍历
    Optional<ITreeNode<K, V>> find(K key);
    int height();
    List<ITreeNode<K, V>> getLeafNodes();
    List<ITreeNode<K, V>> getNodesByDepth(int depth);  // depth 需 > 0
    void insert(K parentKey, ITreeNode<K, V> newNode);
    boolean delete(K key);
}

public interface ITreeNode<K, V> {
    K getKey();
    V getValue();
    void setValue(V value);
    List<ITreeNode<K, V>> getChildren();
    void setChildren(List<ITreeNode<K, V>> children);
}
```

`AbstractTree` 提供模板（根节点与 size 管理），子类实现 `insertNode` / `deleteNode`；`MultiwayTree` 为递归实现，`SimpleTreeNode` 为默认节点（children 默认 `LinkedList`）。

### 使用示例

```java
ITree<String, Object> tree = new MultiwayTree<>();

ITreeNode<String, Object> root = new SimpleTreeNode<>("root", "Root Node");
tree.setRoot(root);                                        // 直接设根（不经过 insert，size 不增加）

tree.insert("root", new SimpleTreeNode<>("child1", "Child 1"));
tree.insert("root", new SimpleTreeNode<>("child2", "Child 2"));
tree.insert("child1", new SimpleTreeNode<>("grandchild", "Grandchild"));

tree.traverse(node -> System.out.println(node.getKey() + ": " + node.getValue()));
Optional<ITreeNode<String, Object>> found = tree.find("child1");
int height = tree.height();                                // 3
List<ITreeNode<String, Object>> leaves = tree.getLeafNodes();
List<ITreeNode<String, Object>> level2 = tree.getNodesByDepth(2);
boolean deleted = tree.delete("child1");                   // 连同子树一并摘除
```

> **内存树使用注意**（源码行为）：
> - `insert` 在根节点为 null 时，无论 `parentKey` 为何值都把新节点作为**根**插入；
> - `setRoot` 不维护 `size`，`size` 仅由 `insert` / `delete` 增减；
> - `delete` 只 `size--` 一次，删除整棵子树时计数偏小；
> - 键重复时 `insertNode` 会向所有匹配分支插入副本（递归无命中判断）。

## 二、闭包表持久化（closure-list-impl）

### ITreeStore 契约

```java
public interface ITreeStore<ID> {
    void store(ITreeStorable treeStorable, String parentNodeId);  // 新增或移动节点
    List<ITreeStorable> getDirectChildren(String nodeId, String group);      // 直接子节点
    List<TreeNodeModel> getParentNodes(String nodeId, String group);         // 祖先链（含自身）
    void delete(String nodeId, String group);                                // 删除节点及整个子树
    List<ITreeStorable> getRootNodes(String treeGroup);                      // 分组内所有根节点
    List<String> listTreeGroups();                                           // 所有树组
    boolean isExist(String nodeId, String group);
    void tryRebuildStructure(String treeGroup);                              // 重建闭包关系
    ITreeStorable getNode(String nodeId, String group);
    List<ITreeStorable> getNodesByEntityId(ID id);
    default ITreeStorable getNodeByEntityIdAndGroup(ID id, String group);    // 默认方法：按实体ID+组过滤
}
```

> 注意：**不存在 `getChildren`（全子树）方法**，取子节点请用 `getDirectChildren`；重建方法名为 `tryRebuildStructure`（非 `buildTreeClosure`）。

节点载体：`ITreeStorable`（`nodeId` / `treeGroup` / `entityId`）——`TreeNodeModel`（含 `depth`）与 `SimpleTreeStrorableModel`（无 depth）两种实现。

### ClosureTreeServiceImpl 行为

**store（新增 / 移动）**：

- 校验节点、节点 ID、树组非空；`parentNodeId` 查不到父节点时按**根节点**插入；
- **新增**：根节点插入自身关系（depth=0）；子节点插入自身关系 + 父子关系（depth=1）+ 复制父节点全部祖先关系并 depth+1；
- **更新（移动）**：树组不一致直接异常；`parentNodeId` 为 null 时**不支持升级为根**（记日志返回）；父节点未变则仅更新 `entityId`；父节点变更时——删除该节点全部旧后代关系 → 重建自身/父子/祖先关系 → 对**每个子孙**删除旧祖先链、按 `newParent.depth + child.depth + 1` 重建新祖先链；
- 全程 `@Transactional(rollbackFor = RuntimeException.class)`。

**delete**：经闭包表取出全部后代 + 自身 → `deleteByIdList` 批量删节点 → 逐节点删除其作为后代与祖先的全部闭包关系；`@Transactional` + `@CacheEvict` 清除该节点缓存。

**getParentNodes**：返回 `TreeNodeModel` 列表，按 `depth` **降序**（距根最远的祖先在前，depth=0 的自身在最后），即"由远及近"的祖先链；**包含节点自身**。

**getDirectChildren**：查询闭包表 `ancestorId=nodeId AND depth=1`，逐个补 `entityId`（会触发节点查询）。

### 缓存

| 缓存名 | 常量 | 应用 |
|--------|------|------|
| `tree_node_entity` | `CacheNamesConstants.TREE_NODE_ENTITY` | `@Cacheable(key = "treeGroup:nodeId")` 于 `getTreeNode`；`delete` 单键失效；`tryRebuildStructure` 全量失效 |

TTL 可在 `tbs.cache.ttl.overrides` 中按 `tree_node_entity` 覆盖（需 cache 模块启用）。

> **两个已知实现细节**（以源码为准）：
> 1. **内部自调用绕过缓存**：`store`/`delete` 等内部均经 `getInstance().getTreeNode(...)` 调用自身，Spring AOP 不生效——`@Cacheable` 仅对**外部经 Bean 注入的调用**生效；
> 2. **`store` 未失效缓存**：写路径不带 `@CacheEvict`，节点移动/更新后 `tree_node_entity` 可能返回旧数据，依赖 TTL 或 `tryRebuildStructure` 全量清除。

### 关系重建（tryRebuildStructure）

用于闭包表损坏后的数据修复：

1. 抢占 JVM 本地 `ReentrantLock`（抢锁失败抛 `BUSINESS_LOGIC_ERROR` 级"正在重建…"）；
2. 在 `CacheUtils` 写入重建锁 `rebuildTreeLock`，随后删除目标树组（组名为空则全表）的全部闭包关系；
3. 加载该组全部节点，按 `nodeId` 建 Map（**重复节点 ID 直接抛异常**）；
4. 逐节点沿 `parentId` 上溯生成关系（`traceRelations`），**检测父指针成环**（`visited` 命中即抛异常）；
5. 按 50 条一批 `insertList` 落库；
6. 事务提交/回滚完成后（`TransactionSynchronization.afterCompletion`）释放缓存锁与本地锁。

**重建期间写保护**：`store` / `delete` / `getParentNodes` 均调用 `rebuildTreeClosureCheck()`，检测到重建锁存在即抛 `BUSINESS_LOGIC_ERROR` 级异常拒绝操作。

### Mapper 自定义 SQL

| Mapper | 方法 |
|--------|------|
| `TreeNodeMapper`（继承 `IFrameworkMapper<TreeNodeEntity, Long>`） | `getNodeById`、`getNodeByNodeIdAndTreeGroup`、`listAllGroup`、`getRootNodeByGroupName`、`getNodesByEntityId` |
| `TreeRelationMapper`（`@Repository`） | `selectAncestorsByDescendantId`、`selectDescendantsByAncestorId`、`updateByAncestorIdAndDescendantId`、`selectByAncestorIdAndDescendantId`、`deleteByAncestorIdAndDescendantId`、`deleteByDescendantId`、`deleteByAncestorId`、`deleteAllRelations`、`deleteRelationsByGroup` |

## 使用示例

```java
@Service
public class DeptTreeService {

    @Resource
    private ITreeStore<Long> treeStore;

    /** 挂载业务实体到树（parentNodeId 为 null 或查无此节点时作为根节点） */
    public void attach(Long deptId, String parentDeptId) {
        SimpleTreeStrorableModel node = new SimpleTreeStrorableModel();
        node.setTreeGroup("department");
        node.setNodeId(String.valueOf(deptId));
        node.setEntityId(deptId);
        treeStore.store(node, parentDeptId);
    }

    public List<ITreeStorable> children(String deptId) {
        return treeStore.getDirectChildren(deptId, "department");
    }

    /** 祖先链：由远及近，最后一项为自身（depth=0） */
    public List<TreeNodeModel> ancestors(String deptId) {
        return treeStore.getParentNodes(deptId, "department");
    }

    /** 按业务实体 ID 反查节点 */
    public ITreeStorable nodeOf(Long deptId) {
        return treeStore.getNodeByEntityIdAndGroup(deptId, "department");
    }

    /** 删除部门及其全部子部门 */
    public void remove(String deptId) {
        treeStore.delete(deptId, "department");
    }

    /** 闭包关系损坏时重建（组名传 null 表示全量重建） */
    public void rebuild() {
        treeStore.tryRebuildStructure("department");
    }
}
```

`shared` 模块的 `AbstractTreeStructService`（system 模块部门服务即基于它）已封装上述调用，业务侧通常直接使用它而非裸调 `ITreeStore`。

## 扩展开发

### 自定义树存储

实现 `ITreeStore<ID>` 并注册为 Bean（闭包表实现无 `@ConditionalOnMissingBean`，多实现并存时请用 `@Primary` 或按名称注入）。

### NoImplTreeStore 陷阱

`NoImplTreeStore`（tree-api 提供）**构造函数即抛 `FormattedRuntimeException("树类未实现")`**——它是"占位实现"而非可用的空实现，仅用于显式表达"未配置树存储"。若某配置默认值指向它（如 system 模块 `tbs.system.remote.tree-store-impl` 默认值），未在配置中替换会在装配阶段直接失败。

## 使用注意事项

1. **依赖坐标不可写父 POM**：`com.tbs:tree-struct` 为聚合 POM，请依赖 `tree-api` 或 `closure-list-impl`
2. **方言由模块提供**：sql 引擎与 `SqlDialect` 来自 `sql-impl` + 方言模块（`sql-mysql-impl` 等）自动装配，应用需依赖其一；缺失方言会导致 `QueryEngine` 装配失败（已不再使用 `@UseMysql` 注解）
3. **`getChildren` 不存在**：取子节点用 `getDirectChildren`；重建用 `tryRebuildStructure`
4. **`NoImplTreeStore` 不可直接实例化**：构造即抛异常，勿将其作为默认 Bean
5. **节点不支持"升级为根"**：`store` 时对已存在节点传 `parentNodeId=null`（或查无父节点）只会记录日志并跳过，不会变为根节点
6. **祖先链排序为由远及近且含自身**：`getParentNodes` 结果最后一项是节点自身（depth=0）
7. **缓存自调用不生效**：`getTreeNode` 的 `@Cacheable` 在内部调用路径上被 AOP 绕过；`store` 也不失效缓存，强一致场景请缩短 `tree_node_entity` 的 TTL 或改写后手动 evict
8. **`getInstance()` 依赖启动顺序**：静态实例由 `IStartup.startUp()` 赋值，容器未就绪时调用会 NPE
9. **重建期间拒绝写入**：`store`/`delete`/`getParentNodes` 会检测重建锁并抛业务异常，重建完成后自动恢复
10. **重建会检测环与重复 ID**：父指针成环或同组重复 `nodeId` 均直接抛异常（事务回滚）
11. **表结构由 jkit-sql-auto 自动维护**：使用 `@UseClosureTableTree` 的应用需在 `jkit.sql.auto.packages` 中包含 `com.tbs.struct.tree.closure.impl.entity`（平台服务默认 `mode: update`）

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

---

**最后更新**: 2026年9月
**版本**: 1.0-SNAPSHOT
