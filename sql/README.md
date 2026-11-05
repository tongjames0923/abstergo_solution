# SQL 模块 - 注解驱动 SQL 查询框架

## 模块概述

SQL 模块（`com.tbs:sql-api` / `sql-impl` / 各方言实现模块）是基于 MyBatis 的**注解驱动 SQL 构建框架**（Java 21），核心目标是消除手写 SQL 与 XML 映射：通过注解描述查询语义，框架自动完成 SQL 生成、参数绑定、结果映射与分页排序。

四种查询方式覆盖不同场景：

| 方式 | 入口 | 适用场景 |
|------|------|----------|
| **DSL 流式查询** | `QueryEngine.getEntityQueryWrapper(Xxx.class).query()` | 类型安全、条件动态的编程式查询 |
| **注解查询对象（QO）** | `Mapper.queryByQO(qo, page, Xxx.class)` | 前端多条件筛选、固定查询契约 |
| **资源 SQL 文件** | 继承 `AbstractResourceCriteria` | 复杂 JOIN / 多方言 SQL，需外置维护 |
| **类级自定义 SQL** | 查询模型标注 `@SelectSQL` | 与实体强绑定的简短固定 SQL |

其他能力：**字段自动填充**（ID/时间）、**SQL 执行观测**（日志/耗时/慢 SQL 告警）、**WHERE 拦截器**（数据权限/逻辑删除）、**多数据库方言**（MySQL/Oracle/PostgreSQL/SQL Server）、**DataSource 装饰与 JDBC 执行拦截**（阻断/放行）、**编程式事务**。

技术底座：`sql-api` 依赖 `mybatis-spring-boot-starter`、`spring-boot-starter-data-jpa`（仅用于 `@Table`/`@Column` 元数据）与 `com.tbs:common`；具体 JDBC 驱动由各数据库方言实现模块携带（如 `sql-mysql-impl` 携带 `com.mysql:mysql-connector-j`，版本由根 pom 管控、已排除仅 X DevAPI 需要的 `protobuf-java`）。通用 CRUD 由 `GenericMapper` 自实现，不依赖外部通用 Mapper 框架。

## 模块结构

```
sql/                              # 聚合 pom
├── sql-api/                      # 契约层（不携带 JDBC 驱动 / 中间件客户端）
│   └── com/tbs/sql/
│       ├── annotation/           #   全部注解（@SelectSQL/@QueryField/@SqlAutoValue/@LogicDeleteEntity…）
│       ├── properties/           #   SqlProperty（tbs.sql）
│       ├── service/              #   IQueryEngine（查询引擎门面）
│       ├── dsl/                  #   流式查询接口 + clause/
│       ├── builder/              #   SqlDriver / ISqlBuildContext / ImportedISqlBuildContext 等契约
│       ├── criteria/             #   IQueryCriteria / IPage / Abstract*Criteria / DefaultEntityCriteria
│       ├── mapper/               #   IFrameworkMapper / GenericMapper / SmartQueryMapper / IRawSqlSelectMapper + GenericSqlProvider / RawSqlSelectProvider / ConditionBuilder
│       ├── dialect/              #   SqlDialect + I{Select,Where,Order,Group,Limit,Insert,Update,Delete}Extractor 接口
│       ├── assembler/            #   ISqlAssembler
│       ├── datasource/           #   JdbcExecutionInterceptor + model（裁决上下文/命令类型）
│       ├── interceptor/          #   SqlComposeInterceptor（WHERE 改写 SPI）
│       ├── provider/             #   IAutoValueProvider / ICustomWhereProvider + IdAutoProvider / DateValueAutoProvider
│       ├── order/                #   IQueryOrderBy / IQueryOrderedModel / OrderedModel
│       ├── metadata/ · model/ · enums/ · constants/ · exception/   # 元数据、模型、枚举、常量、异常
│       └── util/                 #   SqlUtils（表名/列名解析）、SqlTextUtils（SQL 文本工具）
├── sql-impl/                     # 核心引擎（依赖 sql-api）
│   └── com/tbs/sql/
│       ├── autoconfig/           #   SqlAutoConfiguration + DataSourceDecorationConfiguration（均 @AutoConfiguration）
│       ├── service/              #   QueryEngine（实现 IQueryEngine）
│       ├── dsl/impl/             #   EntityQueryBuilder / EntityWhereClause 等 DSL 实现
│       ├── builder/              #   AbstractSqlDriver / EntitySqlDriver
│       ├── assembler/impl/       #   DefaultSqlAssembler
│       ├── dialect/extractor/    #   Common*Extractor（含 CommonAssembler）
│       ├── datasource/           #   DataSource 装饰 + JDBC 执行拦截 impl
│       ├── interceptor/          #   SqlAutoFillInterceptor / SqlLogicDeleteIntercepter
│       ├── order/impl/           #   Asc/Desc/OrderedModelOrderImpl
│       ├── util/                 #   TransactionUtil（编程式事务）
│       └── i18n/ · resources     #   SqlMessageSourceProvider + messages-sql*.properties + AutoConfiguration.imports
├── sql-mysql-impl/               # MySQL 方言：MysqlDialect + MysqlLimitExtractor + MysqlDialectAutoConfiguration + mysql 驱动
├── sql-oracle-impl/              # Oracle 方言：OracleDialect + INSERT ALL/LIMIT 提取器 + OracleAssembler + 自动配置
├── sql-postgresql-impl/          # PostgreSQL 方言：PostgreSqlDialect + SELECT/LIMIT 提取器 + 自动配置
└── sql-sqlserver-impl/           # SQL Server 方言：SqlServerDialect + INSERT/LIMIT 提取器 + SqlServerAssembler + 自动配置
```

## 快速开始

### 1. 依赖引入

引擎模块 + 一个数据库方言模块（决定 `SqlDialect` 与 JDBC 驱动）：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>sql-impl</artifactId>
</dependency>
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>sql-mysql-impl</artifactId>
</dependency>
```

### 2. 启用（自动配置，无需注解）

引入 `sql-impl` 与某个方言模块即自动装配：`SqlAutoConfiguration` 注册引擎/拦截器/事务管理并扫描 `com.tbs.sql.mapper`；方言模块的 `*DialectAutoConfiguration` 提供 `SqlDialect`、八个 Extractor 与 `ISqlAssembler` Bean。应用启动类**不再需要** `@EnableTbsSql` / `@UseMysql` 等注解。

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

> 换数据库只换方言模块（`sql-oracle-impl` / `sql-postgresql-impl` / `sql-sqlserver-impl`）；若同时引入多个方言模块，各方言 `SqlDialect` Bean 带 `@ConditionalOnMissingBean`，只装配先注册的一个。

### 3. DSL 流式查询

```java
// 条件 + 排序 + 分页（pageOf 页码从 1 开始）
List<User> users = QueryEngine.getEntityQueryWrapper(User.class).query()
    .where()
    .equal(User::getStatus, 1)
    .like(User::getName, "张")
    .andInclude(w -> w.greaterThan(User::getAge, 18).or().equal(User::getVip, true))
    .pour()
    .getOrderSupport().ifPresent(o -> o.orderByDesc(User::getCreateTime).orderByAsc(User::getName))
    .getPageLimitSupport().ifPresent(p -> p.pageOf(1L, 10L))
    .fetch();

// 单条与计数
User one = QueryEngine.getEntityQueryWrapper(User.class).query().where()
    .equal(User::getId, 1L).pour().fetchOne();
Integer total = QueryEngine.getEntityQueryWrapper(User.class).query().where()
    .equal(User::getStatus, 1).pour().count();
```

实体需标注 JPA `@Table`（表名）与 `@Column`（列名），框架据此生成 SELECT。未声明列名时按驼峰转下划线推导，与 Hibernate 物理命名策略一致；父类声明的字段同样参与映射。

## SQL 构建流水线

```
查询条件来源（DSL where / @QueryField 注解 / 原始 SQL）
        ↓
WhereCompose（条件树：WhereNode 递归组合，节点内 ConnectorEnum 连接）
        ↓  SqlComposeInterceptor 链（按 order 升序，可改写条件树：数据权限、逻辑删除）
        ↓
SqlDriver（EntitySqlDriver 等）装配 SelectInfo / WhereInfo / OrderInfo / GroupInfo / LimitInfo
        ↓
SqlDialect 五个 Extractor 分别生成 SQL 片段（方言差异封装在此）
        ↓
ISqlAssembler 按 SELECT → WHERE → GROUP BY → ORDER BY → LIMIT 顺序拼接
        ↓
FetchMapper → IRawSqlSelectMapper 执行 → Map 转实体（BeanUtil.mapToBean）
```

参数绑定一律走**预编译占位符** `#{_prs.xxx}`：条件值只进入参数映射，不会以字面量出现在 SQL 文本中（日志里的语句因而也不含业务值）。

## 一、DSL 流式查询

### 入口：QueryEngine

```java
QueryEngine.getEntityQueryWrapper(User.class);              // 无参构造创建实体
QueryEngine.getEntityQueryWrapper(User.class, User::new);   // 自定义实体供应商
QueryEngine.getEntityQueryWrapper(existingUser);            // 复用已有对象（自动继承字段值）
```

`QueryEngine` 实现 `IStartup`，启动时收集容器内全部 `SqlComposeInterceptor` 并按 order 排序；`QueryEngine.getInstance()` 为静态单例（未初始化时调用会抛异常）。

### IQueryBuilder 方法

| 方法 | 说明 |
|------|------|
| `where()` | 进入 WHERE 子句构建 |
| `getOrderSupport()` / `getPageLimitSupport()` | `Optional` 包装的排序/分页子句 |
| `groupBy(SFunction)` | 分组字段 |
| `access(SBiConsumer, P)` / `op(SConsumer)` | 设置实体属性 / 对实体执行自定义操作 |
| `toSql()` / `toSqlWithParams()` | 导出 SQL（含参数），用于子查询 |
| `getEntity()` | 获取当前实体 |

### WHERE 子句（IWhereClause）

支持"子语句对象"与"扁平默认方法"两种写法，二者等价：

| 子语句 | 说明 | 扁平默认方法示例 |
|--------|------|------------------|
| `comparison()` | 比较运算 | `equal` / `notEqual` / `greaterThan` / `greaterThanOrEqual` / `lessThan` / `lessThanOrEqual` |
| `like()` | 模糊匹配 | `like` / `notLike` / `leftLike`(LLIKE) / `rightLike`(RLIKE) |
| `in()` | 集合运算 | `in` / `notIn` |
| `nullable()` | 空值判断 | `isNull` / `isNotNull` |
| `sub()` | 嵌套条件组 | `andInclude(Consumer)` / `orInclude(Consumer)` / `include(Consumer, connector)` |
| `logic()` | 逻辑连接符 | `and()` / `or()` |
| `subQuery()` | 子查询条件 | EXISTS / IN SUBQUERY 等 |

所有子语句与扁平方法均通过 `pour()` 返回 `IQueryBuilder` 继续链式调用。

### 排序与分页

```java
// 排序：orderBy(字段, OrderEnum) 或快捷 orderByAsc / orderByDesc
.getOrderSupport().ifPresent(o -> o.orderBy(User::getAge, OrderEnum.DESC));

// 分页：limit(offset, count) 或 pageOf(page, count)（页码从 1 开始）
.getPageLimitSupport().ifPresent(p -> p.limit(0L, 10L));
.getPageLimitSupport().ifPresent(p -> p.pageOf(2L, 10L));   // 等价 limit(10, 10)
```

### 结果获取（IQueryFetcher）

| 方法 | 返回 | 说明 |
|------|------|------|
| `fetch()` | `List<T>` | 无结果返回空列表 |
| `fetchOne()` | `T` | 无结果返回 null |
| `count()` | `Integer` | 总数统计 |

### 枚举定义

- **ContrastEnum（19 个）**：`EQUAL`、`NOT_EQUAL`、`GREATER`、`LESS`、`GREATER_EQUAL`、`LESS_EQUAL`、`IS_NULL`、`IS_NOT_NULL`、`LIKE`、`LLIKE`（左模糊）、`RLIKE`（右模糊）、`NOT_LIKE`、`IN`、`NOT_IN`、`EXISTS`、`NOT_EXISTS`、`IN_SUBQUERY`、`NOT_IN_SUBQUERY`
- **ConnectorEnum**：`AND` / `OR`
- **OrderEnum**：`ASC` / `DESC`，附 `getOrder(String)`（非 "desc" 一律返回 ASC）与 `getOrder(OrderEnum)`（返回 "asc"/"desc"）

## 二、注解驱动查询对象（QO）

### 注解一览

| 注解 | 位置 | 属性（默认值） | 说明 |
|------|------|----------------|------|
| `@QueryField` | 字段 | `index(-1)`、`connector(AND)`、`contrast(EQUAL)`、`ignoreCase(false)`、`ignoreNull(true)`、`customWhereProvider(ICustomWhereProvider)` | 查询条件定义，`@Repeatable`（配合 `@QueryFields` 可重复标注） |
| `@QueryMapping` | 字段 | `value("")` | 字段 → 数据库列名映射 |
| `@OrField` | 字段 | — | 与前一条件以 OR 连接（默认 AND） |
| `@QueryIndex` | 字段 | `index(1000)` | 条件在 WHERE 中的顺序，越小越靠前 |
| `@QueryOrderField` | 字段 | `mappedName("")`、`order(OrderConstants.ASC)` | 排序字段；字段值为 null 时跳过 |
| `@SelectSQL` | 类 | `value("")` | 直接指定 SELECT，命中后不再自动生成、也不要求 `@Table`（边界见"使用注意事项"） |

列名解析优先级（`SqlUtils.getFieldNameWithMapping`）：**`@Column` > `@QueryMapping` > 驼峰转下划线**。最后一级与 Hibernate 的 `CamelCaseToUnderscoresNamingStrategy` 完全一致，未显式声明列名时运行期 SQL 与建表列名不会偏差。

### 查询条件类体系

```
IQueryCriteria（getSelectSQLInfo() → SelectInfoModel）
├── AbstractEntityCriteria        # 基于实体类元数据自动生成
│   └── DefaultEntityCriteria     # 直接用 new DefaultEntityCriteria(User.class)
├── AbstractResourceCriteria      # 从 classpath SQL 文件加载（UTF-8，全局缓存）
└── AbstractRemoteConfigQueryCriteria  # provider 中立的“远程配置读取 SQL”契约
```

> 具体配置源的实现（如从 Nacos 读取 SQL）不落在 sql 框架模块，由使用方模块（shared-pojo 的 `NacosConfigQueryCriteria`）继承 `AbstractRemoteConfigQueryCriteria` 落地，避免框架硬绑定中间件客户端。

实体元数据由 `EntityMetadataExtractor.pullFromClass()` 提取（`@Table` 表名 + 非 static/final/非 `@Transient` 字段，**含父类声明字段**），带类级缓存（`clearCache()` 可清除）。`pullFromClass` 要求实体必须有 `@Table`；`GenericMapper` 走的 `pullTableMetadata` 同样识别父类字段，且表名缺失 `@Table` 时按类名驼峰转下划线推导。

### 完整示例

```java
public class UserQuery extends DefaultEntityCriteria {

    public UserQuery() { super(User.class); }

    /** 用户名模糊，第 1 个条件 */
    @QueryIndex(index = 1)
    @QueryMapping("user_name")
    @QueryField(contrast = ContrastEnum.LIKE)
    private String nameLike;

    /** 状态 IN 查询，忽略空值 */
    @QueryIndex(index = 2)
    @QueryMapping("status")
    @QueryField(contrast = ContrastEnum.IN)
    private List<Integer> statusList;

    /** 创建时间范围（同一字段两个条件，@Repeatable） */
    @QueryIndex(index = 3)
    @QueryMapping("create_time")
    @QueryField(contrast = ContrastEnum.GREATER_EQUAL)
    @QueryField(contrast = ContrastEnum.LESS_EQUAL, index = 4)
    private Date startTime;

    /** 按创建时间倒序（字段值非 null 才生效） */
    @QueryOrderField(mappedName = "create_time", order = OrderConstants.DESC)
    private Boolean orderByCreateTime = true;
}
```

```java
// 执行查询
IPage page = new IPage() {
    @Override public long from() { return 0; }
    @Override public long number() { return 10; }
};

List<User> users = userMapper.queryByQO(query, page, User.class);   // page 传 null 表示不分页
Integer total   = userMapper.countByQO(query, User.class);
```

自定义 SQL 条件（如调用数据库函数）：

```java
@Component
public class AgeRangeProvider implements ICustomWhereProvider<Integer> {
    @Override
    public String getConditionSql(Integer value) {
        return "TIMESTAMPDIFF(YEAR, birthday, CURDATE()) >= " + value;   // 注意入参需自行防注入
    }
}

// @QueryField(customWhereProvider = AgeRangeProvider.class)
```

## 三、Mapper 增强

### IFrameworkMapper

业务 Mapper 统一继承：

```java
@Repository
public interface UserMapper extends IFrameworkMapper<User, Long> {
    // 可继续声明自定义 SQL 方法（@Select / @SelectProvider）
}
```

继承的能力：`GenericMapper`（自研通用 CRUD，基于 `@Table`/`@Column`/`@Id`/`@Transient` 元数据生成 SQL）、`SmartQueryMapper`（条件查询）：

| 方法 | 说明 |
|------|------|
| `insert(entity)` / `insertSelective(entity)` | 单条插入，后置方法跳过 null 字段 |
| `insertList(records)` | 批量插入，按方言容量上限自动分批（非原子，写路径需调用方事务） |
| `selectByPrimaryKey(id)` / `selectByIdList(idList)` | 按主键查询，后者按 IN 上限自动分批 |
| `select(entity)` / `selectCount(entity)` | 按非空、非空串字段等值查询与计数（全空查全表） |
| `updateByPrimaryKey(entity)` / `updateByPrimaryKeySelective(entity)` | 按主键全字段/非空字段更新 |
| `deleteByPrimaryKey(id)` / `delete(entity)` / `deleteByIdList(idList)` | 按主键、条件、主键列表删除（条件删除同样跳过空串，全空等价删全表） |
| `queryByQO(qo, page, entityClass)` | 条件 + 分页查询 |
| `queryByQO(context, qo, page, entityClass)` | 指定 SQL 上下文 |
| `countByQO(qo, entityClass)` | 条件计数（返回 `Integer`） |
| `query(entityClass)` | 直接获得 `IQueryBuilder`（DSL 入口） |

#### GenericMapper 的四库兼容策略

| 能力 | 策略 |
|------|------|
| 单行 INSERT/UPDATE/DELETE | 四库语法一致，无分支 |
| 批量 INSERT | `IInsertExtractor` 分支：标准多行 `VALUES`（MySQL/PG/SQL Server）与 Oracle 的 `INSERT ALL` |
| 容量上限 | 按方言声明：SQL Server 1000 行 / 2100 参数，Oracle 500 行 / 999 参数，超限自动分批 |
| IN 列表 | 统一按 1000 项分批（Oracle 的 ORA-01795 硬限制） |
| 主键 | 应用侧雪花生成，不开 `useGeneratedKeys`，插入后实体上直接可取 |

> 实体主键统一使用 `@SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)`，不再使用 `@GeneratedValue`。雪花 Long 由 web 模块的 `JacksonCustomConfig` 全局序列化为字符串，前端无精度风险。

#### GenericMapper 的语义约定

| 约定 | 说明 |
|------|------|
| 条件字段 | `select` / `selectCount` / `delete` 只拼非 null、非空串、非集合/数组的字段；全空即无 WHERE 全表 |
| 选择性写入 | `insertSelective` / `updateByPrimaryKeySelective` 只跳过 null，空串会被写入 |
| 单批方法 | `insertBatch` / `selectByIdListBatch` / `deleteByIdListBatch` 仅供分批方法内部调用，传空集合直接断言中断 |
| 分批原子性 | `insertList` / `deleteByIdList` 分批执行；需整体回滚时由调用方开事务，明确容忍部分成功的批量写入（日志/埋点）可不开，但需在写入处注释说明理由 |
| 结果映射 | SELECT 统一带 `列名 AS 属性名` 别名，`@Column` 与属性名不一致也能映射 |
| 逻辑删除 | `@LogicDeleteEntity` 实体不得用 `select` / `selectCount` / `delete`（不注入 flag 过滤），读写走 QueryEngine |

### IRawSqlSelectMapper

框架内部执行的最终出口，也可直接注入使用（结果统一为 `Map`）：

```java
@Resource
private IRawSqlSelectMapper rawSqlSelectMapper;

List<Map<String, Object>> rows = rawSqlSelectMapper.fetch(sql, params);
Integer count = rawSqlSelectMapper.count(sql, params);
```

## 四、字段自动填充

MyBatis 插件 `SqlAutoFillInterceptor`，拦截 `Executor#update` 获取 SQL 类型（INSERT/UPDATE），在 SQL 生成前回填参数对象字段值（仅 INSERT/UPDATE 触发，SELECT/DELETE 跳过）。

| 注解 | 说明 |
|------|------|
| `@SqlAutoValue` | `value`（Provider 类）、`insertOnly(false)`（仅 INSERT 填充）、`fillWhenNull(true)`（仅值为 null 时填充） |
| `@AutoSetDate` | 等价于 `@SqlAutoValue(value = DateValueAutoProvider.class)`，无额外属性 |

内置 Provider：

| Provider | 支持类型 | 行为 |
|----------|----------|------|
| `DateValueAutoProvider` | `Date`、`LocalDateTime` | 值为 null 时填当前时间 |
| `IdAutoProvider` | `Long`、`String` | 经 `UuidUtil` 生成雪花 ID（Long）或 UUID 字符串 |

```java
@Table(name = "sys_demo")
public class Demo {

    @Id
    @SqlAutoValue(value = IdAutoProvider.class, insertOnly = true)  // 仅新增生成
    private Long id;

    @AutoSetDate                                                    // 创建时间（Date/LocalDateTime 皆可）
    private Date createTime;

    @SqlAutoValue(value = DateValueAutoProvider.class)              // 新增与更新都刷新
    private LocalDateTime updateTime;
}
```

实现细节：字段扫描递归父类并缓存；Provider 实例双重检查锁缓存；支持 MyBatis `Map` 入参（集合/数组递归下钻，按对象身份去重）；**填充抛异常时直接中断本次写入**（以 `sql.autofill.field.failed` 报错），不会降级为告警日志而插入主键为 null 的脏数据。

> 注意：`SqlAutoFillInterceptor` 在 `SqlAutoConfiguration` 中**无条件注册**（无开关配置），引入 `sql-impl` 后自动装配即生效。

## 五、拦截器与日志

| 组件 | 用途 |
|------|------|
| `SqlComposeInterceptor` | WHERE 条件树改写（`intercept(WhereCompose, ISqlBuildContext)`，`Ordered` 排序）。典型用法：数据权限、租户隔离、逻辑删除（shared 模块的 `SqlLogicDeleteIntercepter` 即为其实现） |
| `SqlExecutionLogInterceptor` | JDBC 执行期 SQL 观测：SQL、耗时与慢 SQL 告警（经 `onExecutionCompleted` 回调输出，见第六节） |

自定义拦截器示例：

```java
@Component
public class TenantInterceptor implements SqlComposeInterceptor {

    @Override
    public WhereCompose intercept(WhereCompose compose, ISqlBuildContext context) {
        // 在条件树上追加 tenant_id 条件
        return compose;
    }

    @Override
    public int getOrder() { return 0; }   // 值越小越先执行
}
```

## 六、DataSource 装饰与 JDBC 执行拦截

在 JDBC 执行层对**全部经过数据源的流量**（含绕过 MyBatis 的 Quartz、批量任务等原生 JDBC 调用）提供执行前拦截扩展点，与其余三层拦截并存各司其职：

| 层次 | 组件 | 职责 |
|------|------|------|
| 构建期 | `SqlComposeInterceptor` | WHERE 条件树改写（数据权限/逻辑删除） |
| MyBatis 执行期 | `SqlAutoFillInterceptor` | Executor 层参数回填 |
| **JDBC 执行期** | DataSource 装饰器（`TbsDataSourceDecorator`） | 全量 JDBC 流量的执行阻断/放行、连接获取拦截 |
| **JDBC 观测** | `SqlExecutionLogInterceptor` | SQL 日志、执行耗时与慢 SQL 告警（替代原 p6spy 驱动代理） |

### 启用

```yaml
tbs:
  sql:
    enable-datasource-intercept: false        # DataSource 装饰总开关，默认关闭
    log-sql: true                             # 全量 SQL 执行日志（INFO），默认 true
    slow-sql-threshold-millis: 2000           # 慢 SQL 告警阈值（毫秒），达到即 WARN
    exclude-sql: []                           # SQL 排除前缀列表，命中即跳过观测与拦截器链
    enable-full-table-update-block: false     # 内置全表 UPDATE/DELETE 防护拦截器开关，默认关闭
```

开启后，容器内的数据源 Bean 被 `DataSourceDecorationPostProcessor` 装饰为 `TbsDataSourceDecorator`
（唯一候选直接装饰；多候选时仅装饰标注 `@Primary` 的；多候选且无 Primary 则跳过并告警）。
此后每次 `getConnection` 均经连接级裁决，每个 Statement 的 execute 系方法均经执行级裁决。

### 自定义拦截器（SPI）

```java
@Component
public class DangerousDdlInterceptor implements JdbcExecutionInterceptor {

    @Override
    public InterceptVerdict interceptConnection(JdbcConnectionContext context) {
        // 连接获取阶段钩子（仅拦截，不做数据源路由）
        return InterceptVerdict.allow();
    }

    @Override
    public InterceptVerdict interceptExecution(JdbcExecuteContext context) {
        if (context.getCommandKind() == SqlCommandKind.DDL) {
            return InterceptVerdict.deny("datasource.intercept.ddl.forbidden");
        }
        return InterceptVerdict.allow();
    }

    @Override
    public int getOrder() { return 0; }   // 值越小越先执行
}
```

要点：

- 注册为 Spring Bean 即生效，按 `getOrder()` 升序链式调用，首个拒绝立即短路
- 拒绝只需返回 `InterceptVerdict.deny(i18nKey, params...)`，框架统一经 `AssertUtil` 抛 i18n 异常（`messages-sql.properties` / `messages-sql_en_US.properties`）
- 内置参考实现 `FullTableUpdateBlockInterceptor`（`enable-full-table-update-block=true` 启用）：拒绝无 WHERE 的 UPDATE / DELETE

### 能力边界（重要）

1. **仅见 SQL 文本**：上下文只含最终 SQL（含 `?` 占位符），不捕获 PreparedStatement 参数值
2. **不改写 SQL**：拦截器只能裁决放行/拒绝，框架不提供 SQL 改写能力
3. **不做路由**：连接级钩子仅作拦截点，不承担多数据源路由职责
4. **覆盖全部 JDBC 流量**：含框架自身语句（连接池健康检查等），误伤时用 `tbs.sql.exclude-sql` 前缀排除
5. **排除规则前置短路**：命中的 SQL 跳过整个拦截器链，亦是性能保护
6. **固有开销**：每个 Connection / Statement 增加一层 JDK 动态代理；无拦截器时仅此开销
7. **批量执行**：`executeBatch` 按 `addBatch` 记录的 SQL 逐条裁决；无记录且有预编译 SQL 时按预编译 SQL 裁决
8. **多数据源**：多候选且未标注 `@Primary` 时跳过装饰并告警，不会误伤次要数据源
9. **观测与裁决分离**：SQL 日志、耗时与慢 SQL 告警由 `onExecutionCompleted` 回调承担，裁决拦截器无须实现观测逻辑

## 七、数据库方言

框架通过 `SqlDialect`（八个 Extractor：Select / Where / Order / Group / Limit / Insert / Update / Delete）抽象数据库差异：

| 方言模块 | 方言 | 分页实现 |
|----------|------|----------|
| `sql-mysql-impl` | `MysqlDialect` | `MysqlLimitExtractor`（LIMIT） |
| `sql-oracle-impl` | `OracleDialect` | `OracleLimitExtractor`（ROWNUM 嵌套） |
| `sql-postgresql-impl` | `PostgreSqlDialect` | `PostgreSqlLimitExtractor` |
| `sql-sqlserver-impl` | `SqlServerDialect` | `SqlServerLimitExtractor`（OFFSET/FETCH） |

除分页外，SELECT/WHERE/ORDER/GROUP 由 `dialect/extractor/` 下的 `Common*Extractor` 统一处理（WHERE 提取器按方言名构造以适配标识符引用差异）。批量 INSERT 只在 Oracle 上有语法差异（`INSERT ALL`），SQL Server 仅收紧容量上限，两者各自覆写 `getInsertExtractor()`。**所有语句的 SQL 生成一律经 `SqlDialect` 的提取器产出（统一管线）**：无方言差异的 SELECT/WHERE/ORDER/GROUP/UPDATE/DELETE 由 `Common*Extractor` 统一提供、各方言默认复用不覆写（UPDATE/DELETE 语法四库一致，仅留 `getUpdateExtractor()`/`getDeleteExtractor()` 与按库覆写的扩展位）；只有出现库间差异（如分页、批量 INSERT）才引入方言专有实现。扩展新数据库：新建独立方言模块（如 `sql-xxx-impl`，依赖 `sql-impl`），实现 `SqlDialect` + 差异 Extractor，参照 `MysqlDialectAutoConfiguration` 编写 `@AutoConfiguration` 配置类并在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 登记，方言 `SqlDialect` Bean 加 `@ConditionalOnMissingBean(SqlDialect.class)`。

## 八、编程式事务

`TransactionUtil`（单例，构造时注入 `PlatformTransactionManager`）：

```java
TransactionUtil.getInstance().executeTransaction(() -> {
    userMapper.insertSelective(user);
    logMapper.insertSelective(log);
});

// 指定传播行为
TransactionUtil.getInstance().executeTransaction(
    TransactionDefinition.PROPAGATION_REQUIRES_NEW, () -> { /* ... */ });
```

异常自动回滚后重新抛出（`TransactionSystemException` / `TransactionException` 原样，其他包装为 `FormattedRuntimeException`）。声明式场景请直接用 `@Transactional`（`SqlAutoConfiguration` 已开启事务管理）。

## 配置说明

```yaml
tbs:
  sql:
    enable-datasource-intercept: false        # DataSource 装饰与 JDBC 执行拦截总开关，默认 false
    log-sql: true                             # 全量 SQL 执行日志（INFO），默认 true；生产可置 false
    slow-sql-threshold-millis: 2000           # 慢 SQL 告警阈值（毫秒），达到即 WARN，默认 2000
    exclude-sql: []                           # SQL 排除前缀列表（忽略大小写），命中即跳过观测与拦截器链
    enable-full-table-update-block: false     # 内置全表 UPDATE/DELETE 防护拦截器开关，默认 false
```

> **WHERE 条件值一律参数绑定**：不存在"把值内联成字面量"的开关。历史上曾有 `tbs.sql.use-raw-value`，但其内联分支依赖的
> 值转换器列表从未被加载（开启即 NPE），且全仓无一处配置，已连同 `IValueConverter` SPI（含两个默认实现与
> `IWhereExtractor#getValueConverters()`）、`@SecretField` 标记、`WhereInfo.useSafe` 豁免链一并下架；存量 yml 里若还写着
> 这个键，绑定阶段会直接忽略（不报错也不生效）。

> **组装器不可通过配置切换**：原 `tbs.sql.sql-assembler-type` 配置项无任何读取点，已作为僵尸配置从 `SqlProperty` 移除（存量 yml 里若还写着它，绑定阶段会直接忽略，不报错也不生效）。
> 实际生效的组装器由各方言自动配置类**硬编码**：MySQL / PostgreSQL 用 `DefaultSqlAssembler`，Oracle 用 `OracleAssembler`、
> SQL Server 用 `SqlServerAssembler`（后两者 `extends CommonAssembler`，仅覆写 `getDefaultOrderSql()` 提供分页必需的默认排序列）。
> 如需自定义组装逻辑，注册 `ISqlAssembler` Bean 时**必须标 `@Primary`**：这些 `@Bean` 上没有 `@ConditionalOnMissingBean`，
> 不加 `@Primary` 会得到两个候选，`SqlAutoConfiguration#queryEngine(SqlDialect, ISqlAssembler)` 的单参数注入会直接启动失败。

## 扩展开发

### 自定义排序策略

```java
@Component(OrderConstants.ORDERED_MODEL)
public class MyOrder implements IQueryOrderBy {
    @Override
    public OrderEnum orderBy(Object target, Field field, String mappedName) {
        return OrderEnum.DESC;   // 返回 null 表示不应用排序
    }
}
```

内置三种：`AscStaticOrder`（Bean 名 `ASC_ORDER_BEAN`）、`DescStaticOrder`（`DESC_ORDER_BEAN`）、`OrderedModelOrderImpl`（`ORDERED_MODEL`，按 `OrderedModel` 值动态决定方向）。`@QueryOrderField(order = ...)` 填的就是这些 **Bean 名称字符串**（`OrderConstants` 常量）。

### 自定义自动填充 Provider

```java
public class TenantIdProvider implements IAutoValueProvider {
    @Override
    public Object getValue(Object target, Field field) { return TenantContext.getTenantId(); }

    @Override
    public boolean support(Class<?> type, Object oldValue) { return String.class.equals(type); }
}
// 使用：@SqlAutoValue(value = TenantIdProvider.class, insertOnly = true)
```

### 自定义 SQL 组装器 / 方言

自定义 `SqlDialect`：方言自动配置的 `sqlDialect` Bean 已带 `@ConditionalOnMissingBean`，你注册任意 `SqlDialect` Bean 即可让方言自动退避，无需 `@Primary`。自定义 `ISqlAssembler`：因各方言装配的 `@Bean` 上没有 `@ConditionalOnMissingBean`，需标注 `@Primary`，否则与方言默认组装器形成两个候选而让 `QueryEngine` 注入失败。另需留意 Oracle / SQL Server 的默认组装器各自覆写了 `getDefaultOrderSql()`，自定义时需自行保留分页所需的默认排序列。

### 清除元数据缓存

实体结构变更后（运行期动态修改），调用 `EntityMetadataExtractor.clearCache(Class)` 或 `clearCache()`。

## 使用注意事项

1. **必须引入一个方言模块**：`sql-impl` 提供引擎但不含 `SqlDialect`；`SqlDialect` Bean 来自方言模块（`sql-mysql-impl` 等）的自动配置，缺失会导致 `QueryEngine` 装配失败
2. **自动填充无开关**：`SqlAutoFillInterceptor` 无条件注册，实体标注 `@SqlAutoValue`/`@AutoSetDate` 即生效（不存在 `tbs.sql.enable-auto-fill-value-interceptor` 配置项）
3. **`@SelectSQL` 只写在类上，且 SQL 内不得自带 WHERE**：`EntityMetadataExtractor.pullFromClass()` 读取类级注解，命中后只回填 `rawSql` 并跳过 `@Table` 与字段清单解析；装配器仍会在其后追加 WHERE / GROUP BY / ORDER BY / LIMIT，逻辑删除与数据权限拦截器产生的条件也会拼到该 SQL 尾部——JOIN 查询里没这个列或列名有歧义时直接报错；列投影（include/exclude）对自定义 SQL 无效；注解写在父类对子类不生效
4. **列名解析优先级为 `@Column` > `@QueryMapping` > 驼峰转下划线**：前两者都不声明时按 Hibernate 规则转下划线，保证运行期列名与建表列名一致
5. **SQL 观测默认开启**：`tbs.sql.log-sql=true` 时全量 SQL 以 INFO 输出；慢 SQL 与执行失败始终 WARN，阈值由 `slow-sql-threshold-millis` 控制
6. **组装器由方言硬编码、无配置项**：`tbs.sql.sql-assembler-type` 已删除（无读取点的僵尸配置）；Oracle/SQL Server 各用方言专用实现，自定义须注册带 `@Primary` 的 `ISqlAssembler` Bean，详见"配置说明"
7. **`@QueryOrderField` 字段值须非 null**：值为 null 的排序字段会被跳过；`mappedName` 为空同样跳过
8. **实体必须有 `@Table`**：`EntityMetadataExtractor.pullFromClass()` 对缺少 `@Table` 的类直接断言失败（使用 `@SelectSQL` 或资源 SQL 的查询对象不受此限）
9. **`QueryEngine.getInstance()` 依赖启动顺序**：静态单例在 `startUp()` 中赋值，过早调用（如其他 `IStartup` 先于本模块执行）会抛"QueryEngine尚未初始化"
10. **`count()` 返回 `Integer`**：超大数据量计数注意溢出
11. **`ICustomWhereProvider` 需自行防注入**：返回的 SQL 片段直接拼接进 WHERE
12. **DataSource 装饰默认关闭**：`tbs.sql.enable-datasource-intercept` 默认 `false`；开启后仅装饰主数据源（唯一候选或标注 `@Primary` 的 Bean），多候选且无 Primary 时跳过装饰并告警
13. **JDBC 执行拦截不拿参数值、不改写 SQL**：拦截上下文仅含 SQL 文本与命令类型推断，SQL 观测（日志/耗时/慢 SQL）经 `onExecutionCompleted` 回调输出
14. **排除规则建议显式配置**：装饰覆盖全部 JDBC 流量，连接池健康检查、批量任务框架自身语句等应通过 `tbs.sql.exclude-sql` 前缀排除（排除同时作用于观测与裁决）
15. **`map-underscore-to-camel-case` 必须为 true**：框架生成的 SQL 带属性名别名，不依赖它；手写 `@Select` 没有别名，靠它把下划线列名映射回驼峰属性。`SqlAutoConfiguration` 在启动期断言，漏配会直接启动失败
16. **`@Table` 只写在最终实体上**：父类字段会进入 SELECT/INSERT 列，但 `Entity::getXxx` 只能引用实体自身声明的字段（`LambdaUtil` 用 `getDeclaredField` 查找），继承字段可用于读写，**不可用于条件与排序**
17. **不存在"条件值内联"开关**：`CommonWhereExtractor` 对所有单值与 IN 集合条件只生成 `#{_prs.param_n}` 占位符，取值永不拼入 SQL 文本（日志里的语句因而也不含业务敏感值）；原 `tbs.sql.use-raw-value` 已随内联分支一同下架

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

---

**最后更新**: 2026年11月
**版本**: 1.0-SNAPSHOT
