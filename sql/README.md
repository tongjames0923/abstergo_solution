# SQL 模块

SQL 模块是一个功能强大的数据库访问框架，基于 MyBatis 和 Spring Data JPA 构建，提供了实体查询、动态 SQL 生成、自动填充、SQL 日志记录等功能。该模块通过函数式编程和链式调用的方式，极大地简化了数据库查询操作。

## 目录结构

```
com.tbs.sql
├── annotation                  # 注解类
│   ├── dialect                # 方言注解
│   │   └── MYSQL.java
│   ├── AutoSetDate.java      # 自动设置日期注解
│   ├── EnableTbsSqlUtils.java # 启用 SQL 工具注解
│   ├── OrField.java          # 或字段注解
│   ├── QueryField.java       # 查询字段注解
│   ├── QueryFields.java      # 查询字段集合注解
│   ├── QueryIndex.java       # 查询索引注解
│   ├── QueryMapping.java     # 查询映射注解
│   ├── QueryOrderField.java  # 查询排序字段注解
│   ├── SecretField.java      # 敏感字段注解
│   ├── SelectSQL.java        # 选择 SQL 注解
│   └── SqlAutoValue.java     # SQL 自动值注解
├── config                      # 配置类
│   ├── MysqlDialectConfig.java # MySQL 方言配置
│   └── SqlConfig.java         # SQL 配置
├── constants                   # 常量定义
│   └── OrderConstant.java     # 排序常量
├── ef                          # 实体查询框架 (Entity Framework)
│   ├── impl                   # 实现类
│   │   ├── EntityPageProvider.java
│   │   ├── EntityQueryBuilder.java
│   │   ├── EntityQueryOrderProvider.java
│   │   ├── EntityQueryWhereProvider.java
│   │   └── SimpleEntityQueryWrapper.java
│   ├── IEntityQueryWrapper.java
│   ├── IFeatureProviderPour.java
│   ├── IQueryFetcher.java
│   ├── IQueryOrderProvider.java
│   ├── IQueryPageProvider.java
│   ├── IQueryQueryBuilder.java
│   └── IQueryWhereProvider.java
├── enums                       # 枚举类
│   ├── QueryConnectorEnum.java # 查询连接器枚举 (AND/OR)
│   ├── QueryContrastEnum.java  # 查询对比枚举 (=, >, <, LIKE等)
│   └── QueryOrderEnum.java     # 查询排序枚举 (ASC/DESC)
├── exception                   # 异常类
│   └── SqlExtractionException.java # SQL 提取异常
├── framework                   # 框架核心
│   ├── assembler              # SQL 组装器
│   │   ├── impl               # 实现类
│   │   │   └── DefaultSqlAssembler.java
│   │   └── ISqlAssembler.java # SQL 组装器接口
│   ├── converter              # 值转换器
│   │   ├── impl               # 实现类
│   │   │   ├── CommonValueConverter.java
│   │   │   └── IterableValueConverter.java
│   │   └── IValueConverter.java # 值转换器接口
│   ├── driver                 # 查询驱动
│   │   ├── impl               # 实现类
│   │   │   ├── AbstractSqlDriver.java
│   │   │   ├── EntityQuerySqlDriver.java
│   │   │   └── ImportedSqlContext.java
│   │   ├── AbstractSqlContext.java
│   │   ├── IQueryDriver.java  # 查询驱动接口
│   │   └── ISqlContext.java   # SQL 上下文接口
│   ├── extractor              # SQL 提取器
│   │   ├── impls              # 实现类
│   │   │   ├── dialect        # 方言实现
│   │   │   │   └── ImportedDialect.java
│   │   │   └── mysql          # MySQL 实现
│   │   │       ├── MysqlGroupExtractor.java
│   │   │       ├── MysqlLimitExtractor.java
│   │   │       ├── MysqlOrderExtractor.java
│   │   │       ├── MysqlSelectExtractor.java
│   │   │       └── MysqlWhereExtractor.java
│   │   ├── IGroupSqlExtractor.java
│   │   ├── ILimitSqlExtractor.java
│   │   ├── IOrderSqlExtractor.java
│   │   ├── ISelectSqlDialect.java
│   │   ├── ISelectSqlExtractor.java
│   │   └── IWhereSqlExtractor.java
│   ├── intercepter            # 拦截器
│   │   ├── ISqlInfoComposeIntercepter.java
│   │   ├── SqlAutoFillIntercepter.java
│   │   └── SqlLoggingInterceptor.java
│   └── model                  # 模型类
│       ├── SqlGroupModel.java
│       ├── SqlLimitModel.java
│       ├── SqlOrderInfo.java
│       ├── SqlSelectInfoModel.java
│       ├── SqlSelectItemModel.java
│       ├── SqlWhereCompose.java
│       ├── SqlWhereInfo.java
│       └── SqlWhereNode.java
├── logger                      # 日志记录
│   ├── impl                   # 实现类
│   │   ├── NoSqlLogger.java
│   │   └── SimpleJsonLogger.java
│   ├── ISqlLogger.java        # SQL 日志接口
│   └── SqlRuntimeStatus.java  # SQL 运行状态
├── mapper                      # 映射器
│   ├── impl                   # 实现类
│   │   ├── FetchMapper.java
│   │   ├── FrameworkSelectProvider.java
│   │   └── RawSqlSelectProvider.java
│   ├── IFrameworkMapper.java  # 框架映射器接口
│   └── IRawSqlSelectMapper.java # 原始 SQL 映射器接口
├── order                       # 排序
│   ├── impl                   # 实现类
│   │   ├── AscStaticOrder.java
│   │   ├── DescStaticOrder.java
│   │   ├── OrderedModelOrderImpl.java
│   │   └── QueryOrderedModel.java
│   ├── IQueryOrderBy.java     # 查询排序接口
│   └── IQueryOrderedModel.java # 查询排序模型接口
├── properties                  # 属性配置
│   └── SqlProperty.java       # SQL 属性
├── provider                    # 提供者
│   ├── impl                   # 实现类
│   │   └── DateValueAutoProvider.java
│   ├── IAutoValueProvider.java # 自动值提供者接口
│   └── ICustomWhereProvider.java # 自定义 WHERE 提供者接口
├── query                       # 查询
│   ├── impl                   # 实现类
│   │   ├── DefaultEntityClassBaseQueryCriteria.java
│   │   └── QueryPageModel.java
│   ├── AbstractEntityClassBaseQueryCriteria.java
│   ├── AbstractResourceBaseQueryCriteria.java
│   ├── IPage.java             # 分页接口
│   └── IQueryCriteria.java    # 查询条件接口
└── utils                       # 工具类
    ├── EntityInfoPuller.java  # 实体信息提取器
    ├── QueryUtil.java         # 查询工具类
    ├── SqlUtils.java          # SQL 工具类
    └── TransactionUtil.java   # 事务工具类
```

## 核心组件说明

### 1. 实体查询框架 (Entity Framework)

实体查询框架是 SQL 模块的核心功能之一，它提供了一种链式调用的方式来构建和执行数据库查询。

#### IEntityQueryWrapper 实体查询包装器

[IEntityQueryWrapper](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/ef/IEntityQueryWrapper.java#L24-L41) 是实体查询的入口点，用于包装实体对象并提供查询功能。

```java
// 创建实体查询包装器
IEntityQueryWrapper<User> wrapper = QueryUtil.getEntityQueryWrapper(User.class);

// 或者使用已有的实体对象
User user = new User();
IEntityQueryWrapper<User> wrapper = QueryUtil.getEntityQueryWrapper(user);
```

#### IQueryQueryBuilder 查询构建器

[IQueryQueryBuilder](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/ef/IQueryQueryBuilder.java#L28-L119) 是查询的核心接口，提供了完整的查询构建功能。

```java
// 获取查询构建器并执行查询
List<User> users = wrapper.query()
    .where()
    .equal(User::getName, "John")
    .and()
    .greaterThan(User::getAge, 18)
    .pour()
    .fetch();
```

#### IQueryWhereProvider 条件提供者

[IQueryWhereProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/ef/IQueryWhereProvider.java#L25-L432) 提供了构建 SQL WHERE 子句的各种方法。

```java
// 多种条件查询示例
List<User> users = wrapper.query()
    .where()
    .like(User::getName, "John")           // LIKE 查询
    .and()
    .between(User::getAge, 18, 65)         // 范围查询
    .and()
    .in(User::getStatus, Arrays.asList("ACTIVE", "PENDING")) // IN 查询
    .and()
    .isNull(User::getDeletedAt)            // IS NULL 查询
    .pour()
    .fetch();
```

#### IQueryOrderProvider 排序提供者

[IQueryOrderProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/ef/IQueryOrderProvider.java#L23-L88) 用于构建查询的 ORDER BY 子句。

```java
// 排序查询示例
List<User> users = wrapper.query()
    .order()
    .orderByDesc(User::getCreatedAt)       // 按创建时间倒序
    .orderByAsc(User::getName)             // 按名称正序
    .pour()
    .fetch();
```

#### IQueryPageProvider 分页提供者

[IQueryPageProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/ef/IQueryPageProvider.java#L19-L59) 用于构建查询的 LIMIT/OFFSET 子句。

```java
// 分页查询示例
IPage page = QueryPageModel.of(1L, 10L); // 第1页，每页10条
List<User> users = wrapper.query()
    .page(page)
    .fetch();
```

### 2. 注解系统

SQL 模块提供了丰富的注解来简化数据库操作。

#### @SelectSQL 注解

用于在实体类上指定自定义 SQL 查询语句：

```java
@SelectSQL("SELECT u.*, d.name as dept_name FROM users u LEFT JOIN departments d ON u.dept_id = d.id")
public class UserWithDepartment {
    private Long id;
    private String name;
    private String deptName;
    // getter 和 setter 方法
}
```

#### @QueryField 注解

用于在查询对象中定义查询字段：

```java
public class UserQuery {
    @QueryField(column = "name", contrast = QueryContrastEnum.LIKE)
    private String name;
    
    @QueryField(column = "age", contrast = QueryContrastEnum.GT)
    private Integer age;
    
    @QueryField(column = "status", contrast = QueryContrastEnum.IN)
    private List<String> statuses;
}
```

#### @SqlAutoValue 注解

用于自动填充字段值：

```java
public class User {
    @SqlAutoValue(DateValueAutoProvider.class)
    private Date createTime;
    
    @SqlAutoValue(DateValueAutoProvider.class)
    private Date updateTime;
}
```

### 3. SQL 驱动和上下文

#### ISqlContext SQL 上下文

[ISqlContext](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/framework/driver/ISqlContext.java#L13-L57) 管理 SQL 查询的上下文信息，包括参数、查询条件等。

#### IQueryDriver 查询驱动

[IQueryDriver](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/framework/driver/IQueryDriver.java#L12-L25) 负责将查询条件转换为实际的 SQL 语句。

### 4. SQL 提取器和方言

SQL 模块支持多种数据库方言，通过提取器来生成特定数据库的 SQL 语句。

#### ISelectSqlDialect SQL 方言接口

[ISelectSqlDialect](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/framework/extractor/ISelectSqlDialect.java#L11-L43) 定义了 SQL 方言的接口，包括各种 SQL 提取器。

#### 提取器接口

- [ISelectSqlExtractor](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/framework/extractor/ISelectSqlExtractor.java#L10-L23): SELECT 语句提取器
- [IWhereSqlExtractor](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/framework/extractor/IWhereSqlExtractor.java#L14-L31): WHERE 语句提取器
- [IOrderSqlExtractor](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/framework/extractor/IOrderSqlExtractor.java#L11-L21): ORDER BY 语句提取器
- [IGroupSqlExtractor](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/framework/extractor/IGroupSqlExtractor.java#L10-L20): GROUP BY 语句提取器
- [ILimitSqlExtractor](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/framework/extractor/ILimitSqlExtractor.java#L9-L19): LIMIT 语句提取器

### 5. 拦截器

#### SqlAutoFillIntercepter 自动填充拦截器

在 SQL 执行前自动填充标记了 [@SqlAutoValue](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/annotation/SqlAutoValue.java#L12-L34) 注解的字段。

#### SqlLoggingInterceptor SQL 日志拦截器

记录 SQL 执行日志，包括执行时间、参数等信息。

### 6. 映射器

#### IFrameworkMapper 框架映射器

扩展了 MyBatis 的 BaseMapper，提供了更多便捷的数据库操作方法。

#### IRawSqlSelectMapper 原始 SQL 映射器

支持执行原始 SQL 语句。

## 配置说明

### 启用 SQL 工具

在 Spring Boot 应用的配置类上添加 [@EnableTbsSqlUtils](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/annotation/EnableTbsSqlUtils.java#L15-L23) 注解：

```java
@SpringBootApplication
@EnableTbsSqlUtils
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### application.yml 配置示例

```yaml
tbs:
  sql:
    logger-type: com.tbs.sql.logger.impl.SimpleJsonLogger
    use-raw-value: false  # 是否使用原始值而非参数化查询
```

## 使用示例

### 1. 基本实体查询

```java
// 定义实体类
@Table(name = "users")
public class User {
    @Id
    private Long id;
    private String name;
    private Integer age;
    private String status;
    // getter 和 setter 方法
}

// 查询所有用户
IEntityQueryWrapper<User> wrapper = QueryUtil.getEntityQueryWrapper(User.class);
List<User> users = wrapper.query().fetch();

// 条件查询
List<User> activeUsers = wrapper.query()
    .where()
    .equal(User::getStatus, "ACTIVE")
    .pour()
    .fetch();
```

### 2. 复杂条件查询

```java
// 复杂条件查询
List<User> users = wrapper.query()
    .where()
    .like(User::getName, "John")
    .and()
    .greaterThanOrEqual(User::getAge, 18)
    .and()
    .lessThan(User::getAge, 65)
    .and()
    .in(User::getStatus, Arrays.asList("ACTIVE", "PENDING"))
    .pour()
    .fetch();
```

### 3. 分页查询

```java
IEntityQueryWrapper<User> wrapper = QueryUtil.getEntityQueryWrapper(User.class);
IPage page = QueryPageModel.of(1L, 10L); // 第1页，每页10条

List<User> users = wrapper.query()
    .where()
    .equal(User::getStatus, "ACTIVE")
    .pour()
    .page(page)
    .fetch();
```

### 4. 排序查询

```java
IEntityQueryWrapper<User> wrapper = QueryUtil.getEntityQueryWrapper(User.class);

List<User> users = wrapper.query()
    .order()
    .orderByDesc(User::getCreatedAt)
    .orderByAsc(User::getName)
    .pour()
    .fetch();
```

### 5. 使用注解进行查询

```java
// 定义查询对象
public class UserQuery implements IQueryCriteria {
    @QueryField(contrast = QueryContrastEnum.LIKE)
    private String name;
    
    @QueryField(contrast = QueryContrastEnum.GREATER_EQUAL)
    private Integer age;
    
    @QueryField(contrast = QueryContrastEnum.IN)
    private List<String> statuses;
    
    // getter 和 setter 方法
}

// 使用查询对象
UserQuery query = new UserQuery();
query.setName("John");
query.setAge(18);
query.setStatuses(Arrays.asList("ACTIVE", "PENDING"));

IEntityQueryWrapper<UserQuery> wrapper = QueryUtil.getEntityQueryWrapper(query);
List<User> users = wrapper.query().fetch();
```

### 6. 自定义 SQL 查询

```java
@SelectSQL("SELECT u.*, d.name as dept_name FROM users u LEFT JOIN departments d ON u.dept_id = d.id WHERE u.status = 'ACTIVE'")
public class ActiveUserWithDepartment {
    private Long id;
    private String name;
    private String deptName;
    // getter 和 setter 方法
}

IEntityQueryWrapper<ActiveUserWithDepartment> wrapper = QueryUtil.getEntityQueryWrapper(ActiveUserWithDepartment.class);
List<ActiveUserWithDepartment> users = wrapper.query().fetch();
```

### 7. 使用 IFrameworkMapper

```java
@Repository
public interface UserMapper extends IFrameworkMapper<User, Long> {
    // 继承了基本的 CRUD 方法
}

// 使用 mapper
@Autowired
private UserMapper userMapper;

public void saveUser(User user) {
    userMapper.insertSelective(user);
}

public User findUserById(Long id) {
    return userMapper.selectByPrimaryKey(id);
}

public List<User> findActiveUsers() {
    User example = new User();
    example.setStatus("ACTIVE");
    return userMapper.select(example);
}
```

## 最佳实践

1. **合理使用注解**：使用 [@QueryField](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/annotation/QueryField.java#L25-L72) 等注解简化查询条件的构建
2. **分页查询**：对于大数据量的查询，始终使用分页避免性能问题
3. **SQL 日志**：开启 SQL 日志便于调试和性能优化
4. **自动填充**：使用 [@SqlAutoValue](file:///Users/abstergo/IdeaProjects/abstergo_solution/sql/src/main/java/com/tbs/sql/annotation/SqlAutoValue.java#L12-L34) 注解自动填充创建时间、更新时间等字段
5. **实体映射**：合理使用 JPA 注解进行数据库表和实体类的映射
6. **避免 N+1 查询**：使用 JOIN 查询或批量查询来避免 N+1 查询问题
7. **使用连接池**：配置合适的数据库连接池参数以提高性能

## 注意事项

1. **事务管理**：在需要事务控制的方法上添加 [@Transactional](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/impl/DefaultUserDetailsServiceImpl.java#L29-L29) 注解
2. **SQL 注入**：使用框架提供的参数绑定机制避免 SQL 注入风险
3. **性能优化**：对于复杂查询，建议编写优化后的原生 SQL
4. **异常处理**：正确处理 SQL 相关异常，如数据重复、外键约束等
5. **连接池配置**：根据应用负载合理配置数据库连接池参数
6. **索引优化**：为经常查询的字段添加数据库索引
7. **批量操作**：对于大量数据操作，使用批量插入、更新等方法提高效率