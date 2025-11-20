# 📁 Etc 模块 - 系统配置与部署资源

## 📋 模块概述

Etc模块是Abstergo解决方案的配置和部署资源目录，包含Docker容器化部署配置、数据库初始化脚本以及系统表单配置等关键资源文件。该模块为整个系统的部署和运维提供基础设施支持。

## 🏗️ 目录结构

```
etc/
├── docker/                          # Docker容器化部署配置
│   └── docker-compose.yml          # Docker Compose编排文件
└── sql_file/                       # 数据库初始化脚本
    ├── form-config/                # 表单配置相关SQL
    │   └── form-configv1.sql       # 表单配置表结构及数据
    └── xxl-job/                    # XXL-Job任务调度SQL
        └── xxl_job_init.sql        # XXL-Job初始化脚本
```

## 🔧 核心功能

### 1. Docker容器化部署

#### Docker Compose配置 (docker-compose.yml)
提供完整的微服务基础设施容器化部署方案：

**支持的服务容器：**
- **Redis缓存服务** (abstergo_redis)
  - 端口: 6379
  - 认证密码: 123456
  - 自动重启策略

- **RabbitMQ消息队列** (abstergo_rabbitmq)
  - 服务端口: 5672
  - 管理界面: 15672
  - 默认用户: admin/admin123

- **Zookeeper注册中心** (abstergo_zookeeper)
  - 客户端端口: 2181
  - 集群端口: 2888/3888
  - 管理端口: 8989

- **Dubbo管理控制台** (abstergo_dubbo_admin)
  - 访问端口: 9600
  - 集成Zookeeper注册中心

- **XXL-Job任务调度中心** (xxl_job_admin)
  - 访问端口: 9898
  - 支持MySQL数据库连接
  - 访问令牌: 123456789

### 2. 数据库初始化脚本

#### XXL-Job任务调度初始化 (xxl_job_init.sql)
完整的XXL-Job分布式任务调度系统数据库初始化脚本：

**核心表结构：**
- **xxl_job_user** - 用户管理表
- **xxl_job_info** - 任务信息表
- **xxl_job_log** - 任务执行日志表
- **xxl_job_group** - 执行器分组表
- **xxl_job_registry** - 执行器注册表
- **xxl_job_lock** - 分布式锁表

**预置任务配置：**
- 通用JSON运行任务接口
- JSON任务帮助接口
- 异步任务支持列表
- 异步任务调用接口

#### 表单配置初始化 (form-configv1.sql)
系统表单配置表结构和预置数据：

**核心功能：**
- **property_value表** - 属性值配置表
- 支持多种表单控件属性配置
- 包含输入框、选择器、日期选择器等控件配置
- 支持国际化占位符文本

**配置特性：**
- 支持JSON属性值转换器
- 包含Element UI组件属性配置
- 预置用户管理、公司管理等表单配置

## 🚀 快速部署

### 环境要求
- Docker 20.10+
- Docker Compose 2.0+
- MySQL 5.7+

### 部署步骤

1. **启动基础设施服务**
   ```bash
   cd etc/docker
   docker-compose up -d
   ```

2. **初始化数据库**
   ```bash
   # 执行XXL-Job初始化
   mysql -h 192.168.50.198 -P 3306 -u root -p < sql_file/xxl-job/xxl_job_init.sql
   
   # 执行表单配置初始化
   mysql -h 192.168.50.198 -P 3306 -u root -p < sql_file/form-config/form-configv1.sql
   ```

3. **验证服务状态**
   - Redis: `redis-cli -h localhost -p 6379 -a 123456 ping`
   - RabbitMQ: 访问 http://localhost:15672 (admin/admin123)
   - Dubbo Admin: 访问 http://localhost:9600
   - XXL-Job: 访问 http://localhost:9898

## ⚙️ 配置说明

### Docker服务配置

#### Redis配置
```yaml
redis:
  image: redis:6.2.19
  ports: ["6379:6379"]
  command: redis-server --requirepass 123456
```

#### RabbitMQ配置
```yaml
rabbitmq:
  image: rabbitmq:3-management
  ports: ["5672:5672", "15672:15672"]
  environment:
    RABBITMQ_DEFAULT_USER: admin
    RABBITMQ_DEFAULT_PASS: admin123
```

#### XXL-Job配置
```yaml
xxl-job-admin:
  image: xuxueli/xxl-job-admin:2.4.0
  environment:
    PARAMS: '--xxl.job.accessToken=123456789 --spring.datasource.url=jdbc:mysql://192.168.50.198:3306/xxlJob?...'
  ports: ["9898:8080"]
```

### 数据库配置

#### XXL-Job数据库连接
- 数据库: xxlJob
- 用户名: root
- 密码: !Ty1997923
- 字符集: UTF-8
- 时区: Asia/Shanghai

#### 表单配置数据库
- 数据库: app_system
- 字符集: utf8mb4
- 支持中文存储

## 🔒 安全配置

### 服务认证
- **Redis**: 密码认证 (123456)
- **RabbitMQ**: 用户名密码认证 (admin/admin123)
- **XXL-Job**: 访问令牌认证 (123456789)

### 网络隔离
- 容器间网络通信
- 端口映射到宿主机
- 支持内网访问控制

## 📊 监控与运维

### 服务监控
- **Redis**: 内置监控命令
- **RabbitMQ**: Web管理界面监控
- **Zookeeper**: 四字命令监控
- **XXL-Job**: 任务执行监控面板

### 日志管理
- 容器日志: `docker-compose logs [service]`
- 服务日志: 各服务内置日志系统
- 任务日志: XXL-Job执行日志记录

## 🔧 故障排查

### 常见问题

1. **容器启动失败**
   ```bash
   # 查看容器状态
   docker-compose ps
   
   # 查看容器日志
   docker-compose logs [service-name]
   ```

2. **数据库连接失败**
   - 检查MySQL服务状态
   - 验证数据库连接信息
   - 检查网络连通性

3. **服务无法访问**
   - 检查端口映射配置
   - 验证防火墙设置
   - 检查服务健康状态

### 性能优化

#### Redis优化
- 配置合理的内存策略
- 设置适当的过期时间
- 启用持久化配置

#### RabbitMQ优化
- 配置消息持久化
- 设置合理的队列长度
- 优化消费者配置

## 📄 版本信息

### 当前版本
- **Docker Compose**: 3.8
- **Redis**: 6.2.19
- **RabbitMQ**: 3-management
- **Zookeeper**: latest
- **XXL-Job**: 2.4.0
- **Dubbo Admin**: latest

### 更新日志
- v1.0.0: 初始版本，包含基础服务容器化配置
- v1.1.0: 新增XXL-Job任务调度支持
- v1.2.0: 完善表单配置和数据库初始化脚本

## 📞 技术支持

如有部署或配置问题，请联系：
- **部署文档**: [部署指南链接]
- **问题反馈**: [技术支持邮箱]
- **社区支持**: [社区论坛链接]

---

**最后更新**: 2024年  
**版本**: 1.0-SNAPSHOT
