# Shared 模块

Shared模块是Abstergo解决方案的核心共享组件库，提供了系统开发中通用的基础设施、数据模型和业务接口。该模块采用Maven多模块架构，包含shared-api和shared-pojo两个子模块，为整个系统提供统一的开发标准和可复用组件。

## 🏗️ 模块架构

### shared-pojo模块
**数据模型与基础组件**
- **实体模型**: 提供系统核心实体类（SystemUserEntity、SystemRoleEntity、SystemCompanyEntity等）
- **基础接口**: 定义IBaseEntity、IIdentifiable、IFlagable等通用接口
- **审计支持**: 实现时间审计(ITimeAuditable)和操作人审计(IOperatorAuditable)
- **工具类**: 提供分页工具(PageUtil)、JSON序列化配置(JacksonConfig)等
- **常量定义**: 包含系统常量、枚举类型和视图配置常量

### shared-api模块
**业务接口与服务模板**
- **控制器模板**: AbstractCrudController提供标准RESTful CRUD接口
- **服务层模板**: 完整的服务模板体系（AbstractService、IServiceTemplate等）
- **数据访问层**: MyBatis Mapper接口定义
- **验证框架**: 自定义验证注解（@UserNameValid、@PasswordValid）
- **拦截器**: 系统操作日志拦截器(SystemOperateInterceptor)

## 📋 核心功能

### 1. 标准CRUD操作
提供基于模板的CRUD操作实现，支持：
- 实体增删改查
- 分页查询
- 逻辑删除/物理删除
- 操作审计

### 2. 数据审计支持
- **时间审计**: 自动记录创建时间和更新时间
- **操作人审计**: 记录操作人信息
- **逻辑删除**: 支持软删除机制

### 3. 验证框架
自定义验证注解：
- `@UserNameValid`: 用户名格式验证
- `@PasswordValid`: 密码强度验证

### 4. 系统管理实体
预定义核心系统实体：
- 用户管理（SystemUserEntity）
- 角色管理（SystemRoleEntity）  
- 公司管理（SystemCompanyEntity）
- 登录信息（SystemLoginInfoEntity）
- 操作记录（SystemOperateRecordEntity）

## 🚀 快速开始

### 依赖配置
在pom.xml中添加依赖：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>shared</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 使用示例

#### 1. 创建实体类
```java
@Entity
@Table(name = "demo_entity")
@Getter
@Setter
public class DemoEntity implements IBaseEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private Date createTime;
    private Date updateTime;
    private Integer flag;
}
```

#### 2. 创建服务类
```java
@Service
public class DemoService extends AbstractFullFeatureServiceTemplate<DemoEntity, Long> {
    
    @Resource
    private DemoMapper demoMapper;
    
    @Override
    protected DemoMapper getMapper() {
        return demoMapper;
    }
    
    @Override
    protected DemoEntity newEntity() {
        return new DemoEntity();
    }
}
```

#### 3. 创建控制器
```java
@RestController
@RequestMapping("/api/demo")
public class DemoController extends AbstractCrudController<DemoEntity, Long, DemoQueryCriteria> {
    
    @Resource
    private DemoService demoService;
    
    @Override
    public IServiceTemplate<DemoEntity, Long> getService() {
        return demoService;
    }
}
```

## 🔧 配置说明

### Jackson序列化配置
模块提供自定义的JSON序列化配置：
- QueryOrderedModel序列化器
- 时间格式统一处理
- 空值处理策略

### 逻辑删除拦截器
通过SqlLogicDeleteIntercepter实现逻辑删除功能：
```java
@Component
public class SqlLogicDeleteIntercepter implements Interceptor {
    // 自动过滤已逻辑删除的记录
}
```

## 📊 数据模型关系

### 用户-角色-公司关系
- 用户与角色：多对多关系（通过UserRoleRelationEntity）
- 用户与公司：多对多关系（通过SystemUserCompanyRelationEntity）
- 角色权限：通过SystemRoleEntity管理

### 审计数据模型
所有实体都支持：
- 创建时间/更新时间自动记录
- 逻辑删除标志位
- 操作人信息记录（可选）

## 🎯 最佳实践

### 1. 实体设计规范
- 所有实体必须实现IBaseEntity接口
- 使用@LogicDeleteEntity注解标记逻辑删除实体
- 合理使用索引优化查询性能

### 2. 服务层开发
- 继承合适的服务模板类
- 实现必要的审计服务支持
- 使用分页查询优化大数据量场景

### 3. 控制器设计
- 继承AbstractCrudController获得标准CRUD接口
- 自定义业务接口时保持RESTful风格
- 统一使用AjaxResult作为响应格式

## 🔄 版本信息

- **Java版本**: 11
- **Spring Boot**: 集成Spring Boot Starter
- **数据库**: 支持JPA和MyBatis
- **构建工具**: Maven

## 📝 开发指南

### 新增实体步骤
1. 在shared-pojo中定义实体类，实现IBaseEntity
2. 在shared-api中创建对应的Mapper接口
3. 实现服务类和控制器类
4. 配置数据库表和索引

### 自定义验证器
```java
public class CustomValidator implements ConstraintValidator<CustomValid, String> {
    // 实现自定义验证逻辑
}
```

## 🤝 贡献指南

欢迎为Shared模块贡献代码！请确保：
1. 遵循现有的代码风格和架构
2. 添加必要的单元测试
3. 更新相关文档
4. 通过代码审查

## 📞 技术支持

如有问题或建议，请联系开发团队或提交Issue。

---

*最后更新: 基于当前代码实现的最新功能说明*
