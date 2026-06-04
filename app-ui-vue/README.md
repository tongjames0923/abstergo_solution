# Abstergo Solution - Vue 管理后台系统

基于 Vue.js 2.x + Element UI 构建的企业级管理后台，提供用户认证、动态路由、角色权限控制、动态表单配置等能力，作为 Abstergo Solution 后端平台的前端管理界面。

## 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 前端框架 | Vue.js | 2.6.10 |
| UI 组件库 | Element UI | 2.15.14 |
| 状态管理 | Vuex | 3.1.0 |
| 路由管理 | Vue Router | 3.0.6 |
| HTTP 客户端 | Axios | 0.18.1 |
| 构建工具 | Vue CLI | 4.4.4 |
| 样式处理 | Sass + SCSS | 1.26.8 |
| CSS 重置 | Normalize.css | 7.0.0 |
| 图标库 | Font Awesome | 7.0.0 |
| CSS 框架 | Bootstrap | 5.3.x |
| 图表库 | ECharts | 6.0.0 |
| 进度条 | NProgress | 0.2.0 |
| 代码规范 | ESLint + eslint-plugin-vue | 6.7.2 / 6.2.2 |
| 单元测试 | Jest + @vue/test-utils | - |
| Mock 数据 | MockJS | 1.0.1-beta3 |

## 功能特性

### 1. 用户认证与 Token 管理
- 登录/登出，支持「记住我」选项
- Token 存储于 localStorage，自动携带 `Authorization` 请求头
- Token 过期自动续期：响应拦截器检测到错误码后触发刷新，并发请求排队等待新 Token
- 未登录（405）自动跳转登录页，刷新失败（468）弹窗提示重新登录

### 2. 动态路由系统
- 静态路由（登录、404、403、首页）与动态路由（从后端接口获取）分离
- 动态路由数据通过 `require.context` 自动映射到 `@/views` 下的 Vue 组件
- 路由数据缓存至 localStorage，30 分钟过期，避免重复请求
- 支持运行时刷新路由缓存（`refreshRoutes`）

### 3. 角色权限控制
- 路由级权限：通过路由 meta.roles 控制页面访问
- 导航守卫自动校验用户角色，不满足则跳转 403
- 登录后获取用户信息（昵称、头像、可用角色），存入 Vuex

### 4. 动态表单系统
- **DynamicForm**：根据 JSON 配置自动渲染表单，支持栅格布局、表单验证、数据收集
- **DynamicFormWithApi**：支持从 API 动态加载表单配置
- **FormEditor**：可视化表单配置编辑器，提供字段配置子组件（InputConfig、DateRangeConfig、DynamicOptionsConfig、UrlImageConfig）
- **FormConfigStore**：Vuex 模块管理表单配置，支持本地配置和后端配置两种来源

### 5. 业务管理模块
- **用户管理**：用户 CRUD、角色分配、登录会话查看、Token 禁用
- **角色管理**：角色 CRUD、启用/禁用
- **公司管理**：公司 CRUD、树形组织架构（支持层级移动）
- **部门管理**：部门 CRUD、树形结构（支持层级移动）
- **系统日志**：操作类型统计报表（ECharts 图表展示）
- **表单管理**：表单配置的创建、编辑、删除、列表查看

### 6. UI 组件能力
- **DynamicTable**：通用动态表格组件
- **CardList**：卡片列表展示
- **QuickUseManagementView**：快捷使用管理视图
- **DynamicOptions**：支持 API 动态加载选项的下拉组件
- **UrlImage**：URL 图片预览与配置组件
- **UserAvatar**：用户头像组件
- **SystemLogCharts**：系统日志 ECharts 图表组件

## 项目结构

```
app-ui-vue/
├── mock/                        # Mock 数据服务
│   ├── mock-server.js           # 开发服务器 Mock 中间件
│   ├── user.js                  # 用户相关 Mock 数据
│   └── table.js                 # 表格相关 Mock 数据
├── public/
│   └── index.html
├── src/
│   ├── api/                     # API 接口定义
│   │   ├── user.js              #   登录/登出/获取用户信息/刷新Token
│   │   ├── admin_user.js        #   管理员用户 CRUD/角色分配/会话管理
│   │   ├── role.js              #   角色 CRUD/启用禁用
│   │   ├── company.js           #   公司 CRUD/树形移动
│   │   ├── department.js        #   部门 CRUD/树形移动
│   │   ├── view.js              #   表单配置 CRUD/动态路由获取
│   │   ├── systemlog.js         #   系统日志报表
│   │   └── table.js             #   表格数据
│   ├── assets/                  # 静态资源
│   ├── components/              # 公共组件
│   │   ├── DynamicForm/         #   动态表单渲染
│   │   ├── FormEditor/          #   表单配置编辑器
│   │   │   └── ConfigComponents/#     字段类型配置子组件
│   │   ├── DynamicTable/        #   动态表格
│   │   ├── DynamicOptions/      #   动态选项下拉
│   │   ├── CardList/            #   卡片列表
│   │   ├── Chart/               #   图表组件
│   │   │   └── js/              #     图表逻辑
│   │   ├── CompanyAdminView/    #   公司管理视图
│   │   ├── DepartmentTreeView/  #   部门树视图
│   │   ├── RoleAdminView/       #   角色管理视图
│   │   ├── UserAdminView/       #   用户管理视图
│   │   ├── QuickUseManagementView/ # 快捷使用管理
│   │   ├── UrlImage/            #   URL 图片组件
│   │   ├── UserAvatar/          #   用户头像组件
│   │   ├── Breadcrumb/          #   面包屑导航
│   │   ├── Hamburger/           #   侧边栏折叠按钮
│   │   └── SvgIcon/             #   SVG 图标
│   ├── icons/                   # SVG 图标
│   │   └── svg/                 #   SVG 源文件（svg-sprite-loader）
│   ├── layout/                  # 页面布局
│   │   ├── index.vue            #   主布局（Sidebar + Navbar + AppMain）
│   │   ├── components/          #   Sidebar / NavBar / AppMain
│   │   └── mixin/               #   ResizeHandler
│   ├── router/                  # 路由配置
│   │   └── index.js             #   静态路由 + 动态路由生成
│   ├── store/                   # Vuex 状态管理
│   │   ├── getters.js           #   全局 getter
│   │   └── modules/
│   │       ├── user.js          #     用户状态（登录/信息/Token刷新）
│   │       ├── app.js           #     应用状态（侧边栏/设备类型）
│   │       ├── settings.js      #     系统设置
│   │       ├── FormConfig.js    #     表单配置管理
│   │       └── routes.js        #     路由数据
│   ├── styles/                  # 全局样式
│   │   ├── variables.scss       #   SCSS 变量（侧边栏主题色）
│   │   ├── mixin.scss           #   SCSS mixin
│   │   ├── index.scss           #   样式入口
│   │   ├── sidebar.scss         #   侧边栏样式（MacOS 风格）
│   │   ├── element-ui.scss      #   Element UI 主题覆盖
│   │   ├── dynamic-form.scss    #   动态表单样式
│   │   └── transition.scss      #   过渡动画
│   ├── utils/                   # 工具函数
│   │   ├── request.js           #   Axios 封装（拦截器/Token刷新队列）
│   │   ├── auth.js              #   Token 存取/记住我状态
│   │   ├── route-cache.js       #   动态路由缓存服务
│   │   ├── validate.js          #   验证工具
│   │   ├── query-help.js        #   查询辅助工具
│   │   ├── get-page-title.js    #   页面标题生成
│   │   └── index.js             #   通用工具（parseTime/formatTime 等）
│   ├── views/                   # 页面视图
│   │   ├── login/               #   登录页
│   │   ├── dashboard/           #   仪表板
│   │   ├── admin/               #   管理页面
│   │   │   ├── user/            #     用户管理
│   │   │   ├── role/            #     角色管理
│   │   │   ├── company/         #     公司管理
│   │   │   └── systemlog/       #     系统日志
│   │   ├── form/                #   表单页面
│   │   │   ├── FormCreator.vue  #     表单创建
│   │   │   ├── FormManager.vue  #     表单管理
│   │   │   └── index.vue        #     表单演示
│   │   ├── table/               #   表格演示
│   │   ├── tree/                #   树形结构演示
│   │   ├── nested/              #   嵌套路由演示
│   │   ├── 403.vue              #   无权限页
│   │   └── 404.vue              #   未找到页
│   ├── App.vue                  # 根组件
│   ├── main.js                  # 入口文件（异步加载路由）
│   ├── permission.js            # 路由导航守卫
│   └── settings.js              # 全局配置
├── tests/unit/                  # 单元测试
│   ├── components/              #   组件测试
│   └── utils/                   #   工具函数测试
├── .env.development             # 开发环境变量（VUE_APP_BASE_API=/api）
├── .env.staging                 # 预发布环境变量（VUE_APP_BASE_API=/stage-api）
├── .env.production              # 生产环境变量（VUE_APP_BASE_API=/prod-api）
├── vue.config.js                # Vue CLI 配置（代理/构建优化/SVG Sprite）
├── babel.config.js              # Babel 配置
├── jest.config.js               # Jest 测试配置
├── postcss.config.js            # PostCSS 配置
└── package.json
```

## 快速开始

### 环境要求

- Node.js >= 8.9
- npm >= 3.0.0

### 安装依赖

```bash
npm install
```

### 开发环境

```bash
npm run dev
```

默认端口 **9528**，可通过环境变量修改：

```bash
port=8080 npm run dev
```

开发环境下，`/api` 前缀的请求会被代理到 `http://localhost:8080`（后端服务地址，可在 `src/settings.js` 的 `backend` 字段修改），同时 Mock 服务会拦截部分接口。

### 构建

```bash
# 生产环境
npm run build:prod

# 预发布环境
npm run build:stage
```

构建产物输出到 `dist` 目录。

### 代码检查

```bash
npm run lint
```

### 单元测试

```bash
npm run test:unit
```

### SVG 优化

```bash
npm run svgo
```

## 配置说明

### 全局配置（src/settings.js）

```js
module.exports = {
  title: '小童快速开发模板',     // 页面标题
  fixedHeader: false,            // 是否固定 Header
  sidebarLogo: false,            // 侧边栏是否显示 Logo
  backend: 'http://localhost:8080', // 后端接口地址
  tokenHeader: 'Authorization',  // Token 请求头名称
  tokenCookieKey: 'SYSTEM_TOKEN', // Token 存储键名
  successCode: 200,              // 成功状态码
  noLoginCode: [405],            // 未登录错误码
  refreshTokenCode: [468],       // 需刷新Token的错误码
  debugCodes: [601],             // 调试信息码
  noticeCodes: [701],            // 通知信息码
  // 表单配置映射 key
  userAddFormKey: '2',
  userSearchFormKey: '1',
  userEditFormKey: '3',
  companySearchFormKey: '4',
  companyAddFormKey: '5',
  companyEditFormKey: '5'
}
```

### 环境变量

| 文件 | ENV | VUE_APP_BASE_API |
|------|-----|------------------|
| `.env.development` | development | `/api` |
| `.env.staging` | staging | `/stage-api` |
| `.env.production` | production | `/prod-api` |

### 开发代理（vue.config.js）

开发服务器将 `/api` 请求代理到 `src/settings.js` 中配置的 `backend` 地址，并自动重写路径前缀。

### ESLint 规范

- 使用 `babel-eslint` 解析器
- 继承 `plugin:vue/recommended` + `eslint:recommended`
- 强制使用 `const`（prefer-const）
- 禁止分号（semi: never）
- 强制使用单引号
- 2 空格缩进

## 核心机制

### 请求拦截器与 Token 刷新

`src/utils/request.js` 封装了 Axios，核心逻辑：

1. **请求拦截**：自动在请求头中携带 Token
2. **响应拦截**：
   - 状态码为 200 → 正常返回
   - 错误码匹配 `refreshTokenCode` → 弹窗要求重新登录
   - 错误码匹配 `noLoginCode` → 触发 Token 刷新流程，并发请求加入等待队列
   - 其他错误码 → 消息提示
3. **Token 刷新队列**：多个并发请求同时触发刷新时，仅执行一次刷新，其余请求等待新 Token 后自动重发

### 动态路由加载流程

```
应用启动 (main.js)
  → bootstrap() 异步执行
  → getServiceRouter()
    → generateRoutesFromServer()
      → 合并 constantRoutes（静态路由）
      → 若有 Token，从缓存/后端获取动态路由
      → mapComponent() 将组件路径映射为实际 Vue 组件
    → new Router({ routes })
    → setUpRouterPermissions() 注册导航守卫
  → Vue 实例挂载
```

### Vuex Store 模块

| 模块 | 职责 |
|------|------|
| `user` | 登录状态、用户信息、Token 管理、动态路由数据 |
| `app` | 侧边栏状态、设备类型 |
| `settings` | 系统设置项 |
| `FormConfig` | 表单配置管理（支持本地/后端两种来源） |
| `routes` | 路由数据存储 |

### 侧边栏主题

采用 MacOS AppStore 风格，通过 SCSS 变量控制配色（`src/styles/variables.scss`）：

- 背景色：`#f0f0f0`
- 激活色：`#0071e3`（Apple Blue）
- 圆角：`12px`
- 侧边栏宽度：`220px`

## 页面视图

| 路径 | 说明 |
|------|------|
| `/login` | 登录页 |
| `/dashboard` | 仪表板（需角色 `2`） |
| `/table` | 表格演示页 |
| `/403` | 无权限提示 |
| `/404` | 页面未找到 |

> 管理页面（用户/角色/公司/部门/表单管理/系统日志）的路由由后端动态下发，角色权限通过 `meta.roles` 控制。

## 部署

1. 构建生产版本：`npm run build:prod`
2. 将 `dist` 目录部署到 Web 服务器
3. 配置 Nginx 反向代理 `/prod-api` 到后端服务

Nginx 参考配置：

```nginx
server {
    listen 80;
    location / {
        root /path/to/dist;
        try_files $uri $uri/ /index.html;
    }
    location /prod-api {
        proxy_pass http://backend-server:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 许可证

MIT License