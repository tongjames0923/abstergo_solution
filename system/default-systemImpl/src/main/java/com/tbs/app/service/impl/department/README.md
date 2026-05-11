# 部门模块说明

## 概述

部门模块是新增的后端功能模块，用于管理系统中的部门信息。该模块采用增量开发方式，不影响现有的公司模块。

## 设计原则

1. **增量修改**：不改动现有公司模块，完全独立的新模块
2. **模仿实现**：部门功能的实现完全模仿公司模块的实现模式
3. **后端优先**：暂时仅实现后端功能，前端界面后续开发

## 数据模型

### 实体关系

- **公司与部门**：一对一关联（一个部门属于一个公司）
- **用户与部门**：多对多关联（一个用户可以属于多个部门，一个部门可以有多个用户）

### 数据库表

#### 1. sys_department（部门表）

```sql
CREATE TABLE `sys_department` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `department_name` VARCHAR(128) NOT NULL COMMENT '部门名称',
    `department_code` VARCHAR(64) DEFAULT NULL COMMENT '部门编码',
    `company_id` BIGINT NOT NULL COMMENT '所属公司ID',
    `manager` VARCHAR(64) DEFAULT NULL COMMENT '部门负责人',
    `description` TEXT DEFAULT NULL COMMENT '部门描述',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `flag` INT DEFAULT 2 COMMENT '数据状态：0-已删除，1-已激活，2-禁用，5-系统',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_department_code` (`department_code`),
    KEY `idx_department_name` (`department_name`),
    KEY `idx_flag` (`flag`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`),
    KEY `idx_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统部门表';
```

#### 2. system_user_department_relation（用户部门关系表）

```sql
CREATE TABLE `system_user_department_relation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `department_id` BIGINT NOT NULL COMMENT '部门ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id_department_id` (`user_id`, `department_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_department_id` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户部门关系表';
```

## 模块结构

```
部门模块
├── shared-pojo
│   ├── enums
│   │   └── SystemDepartmentFlagEnum.java          # 部门状态枚举
│   ├── entity
│   │   ├── SystemDepartmentEntity.java             # 部门实体
│   │   └── SystemUserDepartmentRelationEntity.java # 用户部门关系实体
│   └── criteria
│       └── CommonSystemDepartmentQueryCriteria.java # 部门查询条件
│
├── shared-api
│   └── mapper
│       ├── SystemDepartmentMapper.java              # 部门Mapper
│       └── SystemUserDepartmentRelationMapper.java  # 用户部门关系Mapper
│
├── system-api
│   └── service.department
│       ├── ISystemDepartmentService.java            # 部门服务接口
│       └── IDepartmentUserRelationService.java      # 部门用户关系服务接口
│
├── default-system-impl
│   ├── service.impl.department
│   │   ├── SimpleDepartmentService.java             # 部门服务实现
│   │   └── SimpleDepartmentUserRelationService.java # 部门用户关系服务实现
│   └── config
│       └── BuisnessBeanConfig.java                  # Bean配置（已更新）
│
└── app
    └── controller.privates
        └── AdminDepartmentController.java           # 部门管理控制器
```

## API接口

### 基础CRUD接口

所有接口路径前缀：`/private/admin/department`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/get/{id}` | 获取部门详情 | 超级管理员 |
| POST | `/save` | 保存部门（新增/更新） | 超级管理员 |
| POST | `/delete/{id}` | 删除部门 | 超级管理员 |
| POST | `/query` | 分页查询部门列表 | 超级管理员 |

### 查询条件

`CommonSystemDepartmentQueryCriteria` 支持以下查询字段：

- `equalsId`: 按ID精确查询
- `likeName`: 按部门名称模糊查询
- `equalsDepartmentCode`: 按部门编码精确查询
- `equalsCompanyId`: 按公司ID精确查询
- `flagEquals`: 按状态标记IN查询
- `createTimeStart/End`: 按创建时间范围查询
- `updateTimeStart/End`: 按更新时间范围查询
- `createTimeOrder`: 按创建时间排序
- `updateTimeOrder`: 按更新时间排序

## 服务接口

### ISystemDepartmentService

继承自 `IServiceTemplate`，提供标准的CRUD操作：

```java
// 查询部门ById
SystemDepartmentEntity queryById(Long id);

// 保存部门
SystemDepartmentEntity save(SystemDepartmentEntity entity);

// 删除部门
SystemDepartmentEntity deleteById(Long id);

// 分页查询
PageVO<SystemDepartmentEntity> queryAndPage(IQueryCriteria criteria, QueryPageModel pageModel);
```

### IDepartmentUserRelationService

提供用户与部门的关系管理：

```java
// 查询用户所属部门
List<SystemDepartmentEntity> queryUserDepartments(Long userId);

// 查询部门所属用户
List<SystemUserEntity> queryDepartmentUsers(Long departmentId);

// 添加用户到部门
void addUserToDepartment(Long userId, Long departmentId);

// 从部门移除用户
void deleteUserFromDepartment(Long userId, Long departmentId);
```

## 使用示例

### 1. 创建部门

```java
@Resource
private ISystemDepartmentService departmentService;

public void createDepartment() {
    SystemDepartmentEntity department = new SystemDepartmentEntity();
    department.setDepartmentName("技术部");
    department.setDepartmentCode("TECH_001");
    department.setCompanyId(1L);  // 所属公司ID
    department.setManager("张三");
    department.setDescription("负责技术研发");
    
    departmentService.save(department);
}
```

### 2. 添加用户到部门

```java
@Resource
private IDepartmentUserRelationService departmentUserRelationService;

public void addUserToDepartment() {
    Long userId = 100L;
    Long departmentId = 50L;
    
    departmentUserRelationService.addUserToDepartment(userId, departmentId);
}
```

### 3. 查询用户的部门

```java
public List<SystemDepartmentEntity> getUserDepartments(Long userId) {
    return departmentUserRelationService.queryUserDepartments(userId);
}
```

### 4. 查询部门的用户

```java
public List<SystemUserEntity> getDepartmentUsers(Long departmentId) {
    return departmentUserRelationService.queryDepartmentUsers(departmentId);
}
```

## 缓存策略

- `user_departments`: 用户部门关系缓存，过期时间3分钟
- 使用 `@Cacheable` 和 `@CacheEvict` 注解管理缓存

## 状态管理

部门状态枚举 `SystemDepartmentFlagEnum`：

- `DELETED (0)`: 已删除
- `ACTIVITED (1)`: 已激活
- `DISABLED (2)`: 禁用
- `SYSTEMED (5)`: 系统内置

新建部门默认状态为 `DISABLED`，需要手动激活。

## 部署步骤

1. **执行SQL脚本**
   ```bash
   mysql -u root -p your_database < etc/sql_file/system_department.sql
   ```

2. **编译项目**
   ```bash
   mvn clean install
   ```

3. **启动应用**
   
   确保配置文件中启用系统模块：
   ```properties
   tbs.system.enable=true
   ```

4. **验证接口**
   
   访问 Swagger 文档查看部门相关接口：
   ```
   http://localhost:8080/doc.html
   ```

## 注意事项

1. **部门编码唯一性**：`department_code` 字段有唯一约束，确保每个部门编码唯一
2. **公司关联**：创建部门时必须指定有效的 `company_id`
3. **逻辑删除**：部门删除采用逻辑删除（修改flag字段），不会物理删除数据
4. **级联删除**：删除部门时不会自动删除用户部门关系，需要手动清理
5. **权限控制**：所有部门管理接口都需要超级管理员权限

## 后续扩展

未来可以考虑的功能扩展：

1. 部门层级结构（父子部门）
2. 部门预算编制
3. 部门绩效考核
4. 前端管理界面
5. 部门数据统计报表
6. 部门权限隔离

## 与公司模块的对比

| 特性 | 公司模块 | 部门模块 |
|------|---------|---------|
| 层级结构 | ✅ 树形结构 | ❌ 扁平结构 |
| 与用户关系 | 多对多 | 多对多 |
| 外部关联 | 无 | 关联公司（一对一） |
| 税号 | ✅ 有 | ❌ 无 |
| Logo | ✅ 有 | ❌ 无 |
| 部门编码 | ❌ 无 | ✅ 有 |
| 负责人 | ❌ 无 | ✅ 有 |

## 技术栈

- Spring Boot
- JPA/Hibernate
- MyBatis
- Spring Cache
- Lombok
- Swagger/OpenAPI 3

## 作者

abstergo

## 版本

1.0.0
