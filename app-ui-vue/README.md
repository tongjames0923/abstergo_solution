# Abstergo Solution - Vue 管理后台系统

## 项目概述

这是一个基于 Vue.js 2.x 构建的企业级管理后台系统，采用现代化的前端技术栈，提供完整的用户管理、权限控制、动态表单配置等功能。系统采用模块化设计，具有良好的可扩展性和维护性。

## 技术栈

- **前端框架**: Vue.js 2.6.10
- **UI组件库**: Element UI 2.15.14
- **状态管理**: Vuex 3.1.0
- **路由管理**: Vue Router 3.0.6
- **HTTP客户端**: Axios 0.18.1
- **构建工具**: Vue CLI 4.4.4
- **样式处理**: Sass
- **图标库**: Font Awesome 7.0.0
- **图表库**: ECharts 6.0.0

## 功能特性

### 1. 用户认证与权限管理
- 完整的登录/登出功能
- 基于角色的权限控制
- Token自动刷新机制
- 路由级别的权限验证

### 2. 动态表单系统
- **表单配置编辑器**: 可视化配置表单字段和属性
- **动态表单组件**: 支持多种字段类型（输入框、选择器、日期范围等）
- **响应式布局**: 自动适配不同屏幕尺寸
- **实时预览**: 配置后即时预览效果

### 3. 管理功能模块
- **用户管理**: 用户信息维护、角色分配
- **角色管理**: 权限配置、角色分配
- **公司管理**: 组织架构管理
- **系统日志**: 运行状态监控和报表展示

### 4. 高级特性
- **动态选项组件**: 支持API动态加载选项数据
- **URL图片组件**: 支持图片URL预览和配置
- **表单数据收集**: 实时获取和验证表单数据
- **响应式设计**: 适配桌面和移动设备

## 项目结构

```
src/
├── api/                 # API接口定义
│   ├── user.js         # 用户相关API
│   ├── role.js         # 角色相关API
│   ├── company.js      # 公司相关API
│   └── systemlog.js    # 系统日志API
├── assets/             # 静态资源
├── components/         # 公共组件
│   ├── DynamicForm/    # 动态表单组件
│   ├── FormEditor/     # 表单配置编辑器
│   ├── Chart/          # 图表组件
│   └── UserAdminView/  # 用户管理组件
├── icons/              # SVG图标
├── layout/             # 布局组件
├── router/             # 路由配置
├── store/              # 状态管理
│   ├── modules/        # 模块化状态
│   └── getters.js      # Getter函数
├── styles/             # 全局样式
├── utils/              # 工具函数
│   ├── auth.js         # 认证工具
│   ├── request.js      # HTTP请求封装
│   └── validate.js     # 验证工具
└── views/              # 页面视图
    ├── admin/          # 管理页面
    ├── dashboard/      # 仪表板
    ├── form/           # 表单页面
    └── login/          # 登录页面
```

## 快速开始

### 环境要求

- Node.js >= 8.9
- npm >= 3.0.0

### 安装依赖

```bash
npm install
```

### 开发环境运行

```bash
npm run dev
```

### 构建生产版本

```bash
# 生产环境构建
npm run build:prod

# 预发布环境构建
npm run build:stage
```

### 代码检查

```bash
npm run lint
```

### 单元测试

```bash
npm run test:unit
```

## 核心功能详解

### 动态表单系统

系统提供完整的动态表单解决方案：

1. **表单配置编辑器** (`FormConfigEditor`)
   - 可视化配置表单字段
   - 支持多种字段类型：输入框、选择器、日期范围、动态选项、URL图片等
   - 实时预览配置效果
   - 字段拖拽排序和复制功能

2. **动态表单组件** (`DynamicForm`)
   - 根据配置自动渲染表单
   - 响应式栅格布局
   - 支持表单验证和数据收集
   - 可折叠/展开的表单卡片

### 权限控制系统

- **路由守卫**: 基于用户角色控制页面访问权限
- **API权限**: 接口级别的权限验证
- **Token管理**: 自动刷新Token机制
- **错误处理**: 统一的权限错误处理

### 图表展示

集成ECharts提供丰富的图表展示：
- 响应时间图表
- 稳定性图表
- 成功率图表
- 访问量统计

## 配置说明

### 环境变量

项目支持多环境配置：
- `.env.development` - 开发环境
- `.env.production` - 生产环境
- `.env.staging` - 预发布环境

### API配置

在 `src/settings.js` 中配置：
- 成功状态码
- Token相关配置
- 请求超时设置

## 开发指南

### 添加新页面

1. 在 `src/views` 目录下创建页面组件
2. 在 `src/router/index.js` 中添加路由配置
3. 配置对应的权限角色

### 添加新API

1. 在 `src/api` 目录下创建API文件
2. 使用统一的 `request` 工具发送请求
3. 处理统一的错误响应

### 自定义组件开发

1. 在 `src/components` 目录下创建组件
2. 遵循Vue组件开发规范
3. 提供完整的Props和Events文档

## 部署说明

### 生产环境部署

1. 构建生产版本：`npm run build:prod`
2. 将 `dist` 目录部署到Web服务器
3. 配置Nginx或其他Web服务器

### Docker部署（可选）

项目支持Docker容器化部署，可参考Dockerfile进行配置。

## 常见问题

### Q: 如何修改主题颜色？
A: 在 `src/styles/element-ui.scss` 中修改Element UI的主题变量。

### Q: 如何添加新的表单字段类型？
A: 在 `DynamicForm` 组件中添加对应的字段类型处理逻辑，并在 `FormConfigEditor` 中配置相应的编辑器。

### Q: 如何配置API接口地址？
A: 在环境变量文件中设置 `VUE_APP_BASE_API` 变量。

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 许可证

MIT License

## 技术支持

如有问题或建议，请联系开发团队。

---

*最后更新: 2024年*