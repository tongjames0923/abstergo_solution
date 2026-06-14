# TBS SQL 框架

一个基于注解驱动的强大SQL查询构建框架，提供类型安全的查询构建、自动SQL生成和灵活的查询配置。

## 功能特性

## 核心功能
- **注解驱动查询**：通过注解定义查询条件，无需手动编写SQL
- **类型安全**：编译时检查查询条件，避免运行时错误
- **多数据库支持**：支持MySQL等主流数据库方言
- **自动SQL生成**：根据注解自动生成优化的SQL语句
- **查询条件组合**：支持AND/OR逻辑组合查询
- **分页查询**：内置分页支持，简化分页逻辑

## 高级特性
- **自定义查询提供器**：支持自定义查询条件逻辑
- **SQL拦截器**：可配置的SQL执行前后拦截
- **自动值填充**：支持自动填充创建时间、更新时间等字段
- **事务管理**：集成Spring事务管理
- **SQL日志**：可配置的SQL执行日志记录

## 快速开始

### 1. 添加依赖

确保在项目中添加了SQL模块依赖：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>sql</artifactId>
</dependency>
```

### 2. 启用框架

在Spring Boot启动类上添加注解：

```java
@EnableTbsSqlUtils
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 3. 配置属性

在`application.yml`中配置SQL相关属性：

```yaml
tbs:
  sql:
    enable-log-interceptor: true          # 启用SQL日志拦截器
    enable-auto-fill-value-interceptor: true  # 启用自动填充值拦截器
    use-raw-value: false                  # 是否使用原始值（默认使用占位符）
```

## 核心注解说明

### @QueryField - 查询字段注解

定义查询条件的核心注解，通常与`@QueryMapping`配合使用：

```java
@Getter
public class UserQuery extends ClassBaseQueryCriteria<User> {
    
    @QueryMapping(value = "username")
    @QueryField(contrast = ContrastEnum.EQUAL, ignoreNull = true)
    private String username;
    
    @QueryMapping(value = "age")
    @QueryField(contrast = ContrastEnum.GREATER_EQUAL)
    private Integer minAge;
    
    @QueryMapping(value = "name")
    @QueryField(contrast = ContrastEnum.LIKE, ignoreCase = true)
    private String searchKey;
    
    @QueryMapping(value = "status")
    @QueryField(contrast = ContrastEnum.IN)
    private List<Integer> statusList;
}
```

**参数说明：**
- `contrast`：比较操作符（等于、大于、LIKE等）
- `connector`：逻辑连接符（AND/OR，默认AND）
- `ignoreCase`：是否忽略大小写
- `ignoreNull`：是否忽略空值
- `customWhereProvider`：自定义查询条件提供器

### @QueryOrderField - 排序字段注解

定义查询结果的排序规则：

```java
public class UserQuery implements IQueryCriteria {
    
    @QueryOrderField(mappedName = "create_time", order = AscStaticOrder.class)
    private Boolean orderByCreateTime;
}
```

### @SelectSQL - 直接SQL查询

支持在实体类上直接定义SQL查询语句，用于自定义查询逻辑：

```java
@SelectSQL("SELECT u.*, d.name as dept_name FROM users u LEFT JOIN departments d ON u.dept_id = d.id")
public class UserWithDepartment {
    private Long id;
    private String name;
    private String deptName;
    // getter 和 setter 方法
}
```

**使用示例：**
```java
// 使用自定义SQL查询实体
IEntityQueryWrapper<UserWithDepartment> wrapper = QueryUtil.getEntityQueryWrapper(UserWithDepartment.class);
List<UserWithDepartment> users = wrapper.query().fetch();
```

**注意：** 当实体类上标注了`@SelectSQL`注解时，框架会优先使用注解中定义的SQL语句，而不会自动生成查询语句。该注解适用于需要复杂JOIN查询或自定义SQL逻辑的场景。

### @AutoSetDate - 自动日期填充

自动填充日期字段：

```java
public class User {
    
    @AutoSetDate
    private Date createTime;
    
    @AutoSetDate(onUpdate = true)
    private Date updateTime;
}
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

### 2. 基于查询类的查询

```java
// 定义查询类，继承ClassBaseQueryCriteria
@Getter
public class UserQuery extends ClassBaseQueryCriteria<User> {
    
    @QueryMapping(value = "name")
    @QueryField(contrast = ContrastEnum.LIKE)
    private String nameLike;
    
    @QueryMapping(value = "age") 
    @QueryField(contrast = ContrastEnum.GREATER_EQUAL)
    private Integer minAge;
    
    @QueryMapping(value = "status")
    @QueryField(contrast = ContrastEnum.IN)
    private List<String> statuses;
    
    // 链式设置方法
    public UserQuery setNameLike(String nameLike) {
        this.nameLike = nameLike;
        return this;
    }
    
    public UserQuery setMinAge(Integer minAge) {
        this.minAge = minAge;
        return this;
    }
    
    public UserQuery setStatuses(List<String> statuses) {
        this.statuses = statuses;
        return this;
    }
}

// 使用查询类
UserQuery query = new UserQuery()
    .setNameLike("John")
    .setMinAge(18)
    .setStatuses(Arrays.asList("ACTIVE", "PENDING"));

// 执行查询
IEntityQueryWrapper<User> wrapper = QueryUtil.getEntityQueryWrapper(User.class);
List<User> users = wrapper.query()
    .where(query)
    .fetch();
```

### 3. 自定义SQL查询

```java
// 使用@SelectSQL注解定义自定义查询
@SelectSQL("SELECT u.*, d.name as dept_name FROM users u LEFT JOIN departments d ON u.dept_id = d.id")
public class UserWithDepartment {
    private Long id;
    private String name;
    private String deptName;
    // getter 和 setter 方法
}

// 执行自定义SQL查询
IEntityQueryWrapper<UserWithDepartment> wrapper = QueryUtil.getEntityQueryWrapper(UserWithDepartment.class);
List<UserWithDepartment> users = wrapper.query().fetch();
```

### 4. 链式条件查询

```java
// 使用链式API构建复杂查询
IEntityQueryWrapper<User> wrapper = QueryUtil.getEntityQueryWrapper(User.class);

// 简单条件查询（默认使用AND连接）
List<User> users = wrapper.query()
    .where()
    .like(User::getName, "John")  // 模糊查询
    .greaterEqual(User::getAge, 18)  // 大于等于
    .in(User::getStatus, Arrays.asList("ACTIVE", "PENDING"))  // IN查询
    .pour()
    .fetch();

// 复杂条件组合（使用include方法）
List<User> complexUsers = wrapper.query()
    .where()
    .like(User::getName, "John")
    .andInclude(where -> where
        .greaterEqual(User::getAge, 18)
        .or()
        .lessThan(User::getAge, 25)
    )
    .pour()
    .fetch();

// 构建复杂的查询条件
List<User> complexQueryUsers = wrapper.query()
    .where()
    .like(User::getName, "John")  // 模糊查询
    .andInclude(where -> where
        .greaterThanOrEqual(User::getAge, 18)  // 大于等于
        .or()
        .equal(User::getStatus, "ACTIVE")  // 等于
    )
    .isNotNull(User::getEmail)  // 非空
    .pour()
    .fetch();

// 更复杂的嵌套条件
List<User> nestedComplexUsers = wrapper.query()
    .where()
    .like(User::getName, "John")
    .orInclude(where -> where
        .andInclude(innerWhere -> innerWhere
            .greaterThanOrEqual(User::getAge, 18)
            .lessThan(User::getAge, 30)
        )
        .equal(User::getStatus, "ACTIVE")
    )
    .pour()
    .fetch();
```

### 5. 分页查询

```java
// 分页查询
IEntityQueryWrapper<User> wrapper = QueryUtil.getEntityQueryWrapper(User.class);

// 获取分页支持
Optional<IQueryPageProvider<User>> pageSupport = wrapper.query().getPageSupport();
if (pageSupport.isPresent()) {
    // 设置分页参数：第1页，每页10条
    List<User> users = pageSupport.get()
        .page(1, 10)
        .fetch();
}
```

## 查询操作符支持

框架支持丰富的查询操作符：

| 操作符 | 说明 | SQL等价 |
|--------|------|---------|
| `EQUAL` | 等于 | `=` |
| `NOT_EQUAL` | 不等于 | `!=`或`<>` |
| `GREATER` | 大于 | `>` |
| `LESS` | 小于 | `<` |
| `GREATER_EQUAL` | 大于等于 | `>=` |
| `LESS_EQUAL` | 小于等于 | `<=` |
| `LIKE` | 模糊匹配 | `LIKE '%value%'` |
| `LLIKE` | 左模糊 | `LIKE 'value%'` |
| `RLIKE` | 右模糊 | `LIKE '%value'` |
| `NOT_LIKE` | 不匹配 | `NOT LIKE` |
| `IN` | 在集合中 | `IN (...)` |
| `NOT_IN` | 不在集合中 | `NOT IN (...)` |
| `IS_NULL` | 为空 | `IS NULL` |
| `IS_NOT_NULL` | 不为空 | `IS NOT NULL` |

## 高级功能

### 自定义查询条件

实现`ICustomWhereProvider`接口创建自定义查询逻辑：

```java
@Component
public class CustomUserProvider implements ICustomWhereProvider {
    
    @Override
    public String provideWhereClause(Object value, String fieldName) {
        // 自定义WHERE条件逻辑
        return "status = 1 AND is_active = true";
    }
}
```

### SQL拦截器

实现`ISqlInfoComposeIntercepter`接口进行SQL拦截：

```java
@Component
public class SecurityInterceptor implements ISqlInfoComposeIntercepter {
    
    @Override
    public void intercept(SqlWhereCompose compose, Class<?> entityClass) {
        // 添加安全过滤条件
        compose.addWhere("tenant_id = ?", getCurrentTenantId());
    }
    
    @Override
    public int getOrder() {
        return 0; // 执行顺序
    }
}
```

## 配置说明

### 数据库方言配置

框架默认支持MySQL方言，如需配置其他数据库：

```java
@Configuration
public class DatabaseConfig {
    
    @Bean
    public ISelectSqlDialect sqlDialect() {
        return new MySQLDialect(); // 或其他数据库方言
    }
}
```

### 事务配置

框架已集成Spring事务管理，可通过`@Transactional`注解使用：

```java
@Service
public class UserService {
    
    @Transactional
    public void updateUser(User user) {
        // 数据库操作
    }
}
```

## 最佳实践

### 1. 查询对象设计
- 为每个查询场景创建专用的查询对象
- 合理使用`ignoreNull`避免不必要的查询条件
- 使用`@QueryIndex`控制字段查询顺序

### 2. 性能优化
- 对常用查询字段建立数据库索引
- 合理使用分页避免大数据量查询
- 使用`select`指定需要的字段

### 3. 安全考虑
- 使用参数化查询防止SQL注入
- 通过拦截器实现数据权限控制
- 对用户输入进行验证和过滤

## 故障排除

### 常见问题

1. **查询条件不生效**
   - 检查是否实现了`IQueryCriteria`接口
   - 确认字段上有正确的注解
   - 检查`ignoreNull`配置

2. **SQL生成错误**
   - 验证数据库方言配置
   - 检查字段名映射是否正确
   - 查看生成的SQL日志

3. **性能问题**
   - 检查是否使用了合适的索引
   - 避免`LIKE '%value%'`全表扫描
   - 使用分页限制结果集大小

### 日志调试

启用SQL日志查看生成的SQL语句：

```yaml
tbs:
  sql:
    enable-log-interceptor: true
logging:
  level:
    com.tbs.sql: DEBUG
```

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

---

**最后更新**: 2026年6月
**版本**: 1.0-SNAPSHOT
