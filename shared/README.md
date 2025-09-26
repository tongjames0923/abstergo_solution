# Shared 模块

Shared 模块包含项目中共享的接口定义和 POJO 实体类，为其他模块提供统一的数据结构和接口规范。

## 目录结构

```
com.tbs.shared
├── interfaces             # 共享接口定义
└── pojo                   # POJO 实体类
```

## 核心组件说明

### 1. Interfaces 模块

Interfaces 模块包含项目中各个模块共享的接口定义，确保模块间的松耦合和一致性。

主要功能：
- 定义服务接口规范
- 提供统一的 API 契约
- 实现模块间的解耦

### 2. POJO 模块

POJO 模块包含项目中使用的实体类和数据传输对象，定义了应用中的核心数据结构。

主要功能：
- 定义实体类（Entity）
- 定义数据传输对象（DTO）
- 定义视图对象（VO）
- 提供数据模型的统一规范

## 自定义使用说明

### 1. 添加依赖

在需要使用共享模块的其他模块的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>shared-interfaces</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>shared-pojo</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 使用共享接口

在实现模块中实现共享接口：

```java
@Service
public class UserServiceImpl implements UserService {
    
    @Override
    public User getUserById(Long id) {
        // 实现获取用户信息的逻辑
        return userRepository.findById(id);
    }
}
```

### 3. 使用共享实体类

在业务代码中使用共享的实体类：

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.create(user);
        return ResponseEntity.ok(createdUser);
    }
}
```

## 最佳实践

1. **接口设计**：
   - 接口设计应遵循高内聚、低耦合原则
   - 接口方法命名应具有明确的业务含义
   - 接口版本管理应考虑向后兼容性

2. **实体类设计**：
   - 实体类应遵循 JavaBean 规范
   - 合理使用注解如 @Data、@Builder 等简化代码
   - 注意实体类的序列化和反序列化

3. **模块划分**：
   - 根据业务领域合理划分包结构
   - 避免循环依赖
   - 保持模块职责单一

4. **版本管理**：
   - 共享模块的变更应考虑对其他模块的影响
   - 重要的接口变更应进行版本控制
   - 发布新版本时应进行充分测试

## 注意事项

1. **依赖管理**：
   - shared 模块应尽量避免依赖其他业务模块
   - 可以依赖基础模块如 common 等
   - 注意依赖的传递性

2. **兼容性**：
   - 避免对接口和实体类进行破坏性修改
   - 新增字段或方法时应考虑向后兼容
   - 删除或修改现有接口时应评估影响范围

3. **文档维护**：
   - 接口和实体类应有清晰的注释说明
   - 重要变更应及时更新相关文档
   - 提供接口使用示例

4. **代码质量**：
   - 遵循统一的代码规范
   - 进行代码审查确保质量
   - 编写必要的单元测试