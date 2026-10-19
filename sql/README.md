# SQL 模块 - 注解驱动 SQL 查询框架

## 模块概述

SQL 模块（`com.tbs:sql`）是基于 MyBatis + tk.mybatis 增强的**注解驱动 SQL 构建框架**（Java 11），核心目标是消除手写 SQL 与 XML 映射：通过注解描述查询语义，框架自动完成 SQL 生成、参数绑定、结果映射与分页排序。

三种查询方式覆盖不同场景：

| 方式 | 入口 | 适用场景 |
|------|------|----------|
| **DSL 流式查询** | `QueryEngine.getEntityQueryWrapper(Xxx.class).query()` | 类型安全、条件动态的编程式查询 |
| **注解查询对象（QO）** | `Mapper.queryByQO(qo, page, Xxx.class)` | 前端多条件筛选、固定查询契约 |
| **资源 SQL 文件** | 继承 `AbstractResourceCriteria` | 复杂 JOIN / 自定义 SQL |

其他能力：**字段自动填充**（ID/时间）、**SQL 执行日志**、**WHERE 拦截器**（数据权限/逻辑删除）、**多数据库方言**（MySQL/Oracle/PostgreSQL/SQL Server）、**DataSource 装饰与 JDBC 执行拦截**（阻断/放行）、**编程式事务**。

技术底座：tk.mybatis `mapper-spring-boot-starter` + `mapper-extra`、`spring-boot-starter-data-jpa`（仅用于 `@Table`/`@Column` 元数据）、`mysql-connector-java`、`p6spy`、`com.tbs:common`。

## 模块结构

```
sql/
└── src/main/java/com/tbs/sql/
    ├── annotation/               # 全部注解（15 个）
    │   ├── core/                 #   @EnableTbsSql（启用）/ @SelectSQL（自定义SQL）
    │   ├── condition/            #   @QueryField / @QueryFields / @OrField / @QueryIndex
    │   ├── mapping/              #   @QueryMapping（列名映射）/ @SecretField（脱敏标记）
    │   ├── autofill/             #   @SqlAutoValue / @AutoSetDate
    │   ├── order/                #   @QueryOrderField
    │   └── dialect/              #   @UseMysql / @UseOracle / @UsePostgreSql / @UseSqlServer
    ├── autoconfig/               # SqlAutoConfiguration + 4 个方言自动配置
    ├── properties/               # SqlProperty（tbs.sql）
    ├── service/                  # QueryEngine（查询引擎，原 QueryUtil）
    ├── dsl/                      # 流式查询接口 + impl/
    ├── builder/                  # SqlDriver / EntitySqlDriver / ISqlBuildContext 等
    ├── criteria/                 # IQueryCriteria / IPage / Abstract*Criteria + impl/
    ├── datasource/               # DataSource 装饰与 JDBC 执行拦截：decoration / interceptor / model
    ├── mapper/                   # IFrameworkMapper / SmartQueryMapper / IRawSqlSelectMapper + impl/
    ├── model/                    # where / select / order / limit / group / query / runtime 模型
    ├── dialect/                  # SqlDialect + 4 种数据库实现 + extractor/
    ├── assembler/                # ISqlAssembler / DefaultSqlAssembler（SQL 片段拼装）
    ├── interceptor/              # SqlAutoFillInterceptor / SqlLoggingInterceptor / SqlComposeInterceptor
    ├── provider/                 # IAutoValueProvider / ICustomWhereProvider + impl/
    ├── logger/                   # ISqlLogger + NoOp/Json/Sql 三种实现
    ├── order/                    # IQueryOrderBy 排序策略 + impl/（Asc/Desc/OrderedModel）
    ├── metadata/                 # EntityMetadataExtractor / FieldMetadataCache（反射缓存）
    ├── constants/                # OrderConstants
    ├── enums/                    # ContrastEnum / ConnectorEnum / OrderEnum
    └── util/                     # SqlUtils（表名/列名解析）/ TransactionUtil（编程式事务）
```

## 快速开始

### 1. 依赖引入

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>sql</artifactId>
</dependency>
```

### 2. 启用（必须同时指定数据库方言）

```java
@SpringBootApplication
@EnableTbsSql   // 导入 SqlAutoConfiguration，开启事务管理，扫描 com.tbs.sql.mapper
@UseMysql       // 必须：提供 SqlDialect Bean，否则 QueryEngine 无法装配
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

`@EnableTbsSql` = `@Import(SqlAutoConfiguration)` + `@EnableTransactionManagement` + `@EnableConfigurationProperties(SqlProperty)`；方言由 `@UseMysql` / `@UseOracle` / `@UsePostgreSql` / `@UseSqlServer` 单独导入（方言配置类同时提供 `SqlDialect`、五个 Extractor 与 `ISqlAssembler` Bean）。

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

实体需标注 JPA `@Table`（表名）与 `@Column`（列名），框架据此生成 SELECT。

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

参数绑定默认走**预编译占位符** `#{_prs.xxx}`（`tbs.sql.use-raw-value=false` 默认，安全）；置 `true` 时改为值内联（**存在 SQL 注入风险**）。

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
| `@SelectSQL` | — | `value("")` | 自定义完整 SQL（见"注意事项"） |
| `@SecretField` | 字段 | — | 敏感字段标记（由日志/序列化实现消费，框架本身不处理） |

列名解析优先级（`SqlUtils.getFieldNameWithMapping`）：**`@Column` > `@QueryMapping` > 字段名**（注意与工具方法 javadoc 描述相反，以代码为准）。

### 查询条件类体系

```
IQueryCriteria（getSelectSQLInfo() → SelectInfoModel）
├── AbstractEntityCriteria        # 基于实体类元数据自动生成
│   └── DefaultEntityCriteria     # 直接用 new DefaultEntityCriteria(User.class)
├── AbstractResourceCriteria      # 从 classpath SQL 文件加载（UTF-8，全局缓存）
└── AbstractRemoteConfigQueryCriteria
    └── NacosConfigQueryCriteria  # 从 Nacos 配置读取 SQL
```

实体元数据由 `EntityMetadataExtractor.pullFromClass()` 提取（`@Table` 表名 + 非 static/final/非 `@Transient` 字段），带类级缓存（`clearCache()` 可清除）。

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

业务 Mapper 统一继承（`@RegisterMapper`）：

```java
@Repository
public interface UserMapper extends IFrameworkMapper<User, Long> {
    // 可继续声明自定义 SQL 方法（@Select / @SelectProvider）
}
```

继承的能力：tk.mybatis `BaseMapper`（CRUD）、`InsertListMapper`（批量插入）、`BatchUpdateMapper` / `BatchUpdateSelectiveMapper`（批量更新）、`IdListMapper`（按 ID 列表查询/删除）、`SmartQueryMapper`（条件查询）：

| 方法 | 说明 |
|------|------|
| `queryByQO(qo, page, entityClass)` | 条件 + 分页查询 |
| `queryByQO(context, qo, page, entityClass)` | 指定 SQL 上下文 |
| `countByQO(qo, entityClass)` | 条件计数（返回 `Integer`） |
| `query(entityClass)` | 直接获得 `IQueryBuilder`（DSL 入口） |

### IRawSqlSelectMapper

框架内部执行的最终出口，也可直接注入使用（结果统一为 `Map`）：

```java
@Resource
private IRawSqlSelectMapper rawSqlSelectMapper;

List<Map<String, Object>> rows = rawSqlSelectMapper.fetch(sql, params);
Integer count = rawSqlSelectMapper.count(sql, params);
```

## 四、字段自动填充

MyBatis 插件 `SqlAutoFillInterceptor`，拦截 `Executor#update` 获取 SQL 类型（INSERT/UPDATE），再在 `ParameterHandler#setParameters` 阶段回填字段值（仅 INSERT/UPDATE 触发，SELECT/DELETE 跳过）。

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

实现细节：字段扫描递归父类并缓存；Provider 实例双重检查锁缓存；支持 MyBatis `Map` 入参（自动跳过 `param1`/`param2`/`collection`/`list`/`array` 内置 key）；`ThreadLocal` 上下文在 finally 中强制清除。

> 注意：`SqlAutoFillInterceptor` 在 `SqlAutoConfiguration` 中**无条件注册**（无开关配置），启用 `@EnableTbsSql` 即生效。

## 五、拦截器与日志

| 组件 | 用途 |
|------|------|
| `SqlComposeInterceptor` | WHERE 条件树改写（`intercept(WhereCompose, ISqlBuildContext)`，`Ordered` 排序）。典型用法：数据权限、租户隔离、逻辑删除（shared 模块的 `SqlLogicDeleteIntercepter` 即为其实现） |
| `SqlLoggingInterceptor` | SQL 执行日志（需 `tbs.sql.enable-log-interceptor=true` 才注册） |
| `ISqlLogger` | 日志输出策略：`NoSqlLogger`（默认无操作）、`SimpleSqlLogger`、`SimpleJsonLogger`；无其他 `ISqlLogger` Bean 时按 `tbs.sql.sql-logger-type` 创建 |

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
| MyBatis 执行期 | `SqlLoggingInterceptor` / `SqlAutoFillInterceptor` | Executor 层日志与参数回填 |
| **JDBC 执行期** | DataSource 装饰器（`TbsDataSourceDecorator`） | 全量 JDBC 流量的执行阻断/放行、连接获取拦截 |
| 驱动层 | p6spy | SQL 格式化观测（保留不动） |

### 启用

```yaml
tbs:
  sql:
    enable-datasource-intercept: false        # DataSource 装饰总开关，默认关闭
    exclude-sql: []                           # SQL 排除前缀列表，命中即跳过拦截器链
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
9. **与 p6spy / Executor 层日志并存**：SQL 日志与耗时观测仍由二者承担，本 SPI 不强制观测回调

## 七、数据库方言

框架通过 `SqlDialect`（五个 Extractor：Select / Where / Order / Group / Limit）抽象数据库差异：

| 启用注解 | 方言 | 分页实现 |
|----------|------|----------|
| `@UseMysql` | `MysqlDialect` | `MysqlLimitExtractor`（LIMIT） |
| `@UseOracle` | `OracleDialect` | `OracleLimitExtractor`（ROWNUM 嵌套） |
| `@UsePostgreSql` | `PostgreSqlDialect` | `PostgreSqlLimitExtractor` |
| `@UseSqlServer` | `SqlServerDialect` | `SqlServerLimitExtractor`（OFFSET/FETCH） |

除分页外，SELECT/WHERE/ORDER/GROUP 由 `dialect/extractor/` 下的 `Common*Extractor` 统一处理（WHERE 提取器按方言名构造以适配标识符引用差异）。扩展新数据库：实现 `SqlDialect` + 各 Extractor，参照 `MysqlDialectAutoConfiguration` 编写配置类并配套 `@UseXxx` 注解。

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

异常自动回滚后重新抛出（`TransactionSystemException` / `TransactionException` 原样，其他包装为 `FormattedRuntimeException`）。声明式场景请直接用 `@Transactional`（`@EnableTbsSql` 已开启事务管理）。

## 配置说明

```yaml
tbs:
  sql:
    enable-log-interceptor: false    # 是否注册 SQL 日志拦截器，默认 false
    sql-logger-type: com.tbs.sql.logger.impl.NoSqlLogger   # 日志器（默认无操作）
    use-raw-value: false             # true 时参数值内联进 SQL（不安全），默认 false 走预编译占位符
    sql-assembler-type: com.tbs.sql.assembler.impl.DefaultSqlAssembler   # 【当前未生效】
    enable-datasource-intercept: false        # DataSource 装饰与 JDBC 执行拦截总开关，默认 false
    exclude-sql: []                           # SQL 排除前缀列表（忽略大小写），命中即跳过拦截器链
    enable-full-table-update-block: false     # 内置全表 UPDATE/DELETE 防护拦截器开关，默认 false
```

> **未生效配置说明**：`sql-assembler-type` 已定义但代码中无任何读取点——`ISqlAssembler` Bean 由各**方言自动配置类**固定创建 `DefaultSqlAssembler`。如需自定义组装逻辑，请直接注册 `ISqlAssembler` 类型的 Bean 覆盖。

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

注册 `ISqlAssembler` 或 `SqlDialect` 类型的 Bean 即可覆盖方言自动配置提供的默认实现。

### 清除元数据缓存

实体结构变更后（运行期动态修改），调用 `EntityMetadataExtractor.clearCache(Class)` 或 `clearCache()`。

## 使用注意事项

1. **方言注解不可省略**：`@EnableTbsSql` 只导入核心配置，`SqlDialect` Bean 来自 `@UseXxx`；缺失会导致 `QueryEngine` 装配失败
2. **自动填充无开关**：`SqlAutoFillInterceptor` 无条件注册，实体标注 `@SqlAutoValue`/`@AutoSetDate` 即生效（不存在 `tbs.sql.enable-auto-fill-value-interceptor` 配置项）
3. **`@SelectSQL` 存在注解目标与读取位置不一致**：注解声明 `@Target(FIELD)`，但 `EntityMetadataExtractor` 从**类**上读取——类级使用无法通过编译，该自定义 SQL 能力实际不可用；复杂 SQL 请改用 `AbstractResourceCriteria`（classpath SQL 文件）
4. **列名解析以 `@Column` 优先**：与工具方法 javadoc 的"@QueryMapping 优先"描述相反，若两注解同时存在，`@Column` 生效
5. **`use-raw-value: true` 有注入风险**：参数值直接内联，仅在调试或完全可信入参场景使用
6. **`sql-assembler-type` 不生效**：见"配置说明"
7. **`@QueryOrderField` 字段值须非 null**：值为 null 的排序字段会被跳过；`mappedName` 为空同样跳过
8. **实体必须有 `@Table`**：`EntityMetadataExtractor.pullFromClass()` 对缺少 `@Table` 的类直接断言失败（使用 `@SelectSQL` 或资源 SQL 的查询对象不受此限）
9. **`QueryEngine.getInstance()` 依赖启动顺序**：静态单例在 `startUp()` 中赋值，过早调用（如其他 `IStartup` 先于本模块执行）会抛"QueryEngine尚未初始化"
10. **`count()` 返回 `Integer`**：超大数据量计数注意溢出
11. **`ICustomWhereProvider` 需自行防注入**：返回的 SQL 片段直接拼接进 WHERE
12. **DataSource 装饰默认关闭**：`tbs.sql.enable-datasource-intercept` 默认 `false`；开启后仅装饰主数据源（唯一候选或标注 `@Primary` 的 Bean），多候选且无 Primary 时跳过装饰并告警
13. **JDBC 执行拦截不拿参数值、不改写 SQL**：拦截上下文仅含 SQL 文本与命令类型推断，观测（SQL 日志/耗时）仍由 p6spy 与 Executor 层日志承担
14. **排除规则建议显式配置**：装饰覆盖全部 JDBC 流量，连接池健康检查、批量任务框架自身语句等应通过 `tbs.sql.exclude-sql` 前缀排除

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

---

**最后更新**: 2026年10月
**版本**: 1.0-SNAPSHOT
