# app-ui-vue - Abstergo Solution 前端管理后台

基于 Vue 2 + Element UI 构建的管理后台前端，作为 Abstergo Solution 后端平台（`app` 模块）的配套管理界面。与后端深度集成：动态路由（对接 `dynamic-view-route` 模块的路由分组与版本管理）、动态表单（表单配置由后端下发）、用户/角色/角色组/部门/公司管理、数据字典、OSS、缓存管理、系统日志报表、应用版本管理、泛化 API 调试等。

## 目录

- [技术栈](#技术栈)
- [环境要求与快速开始](#环境要求与快速开始)
- [构建与测试命令](#构建与测试命令)
- [核心机制](#核心机制)
- [请求封装与令牌刷新](#请求封装与令牌刷新)
- [国际化](#国际化)
- [API 模块清单](#api-模块清单)
- [事件编排页面](#事件编排页面)
- [项目结构](#项目结构)
- [配置说明](#配置说明)
- [开发代理与后端联调](#开发代理与后端联调)
- [部署](#部署)
- [调试工具](#调试工具)
- [注意事项](#注意事项)

## 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 前端框架 | Vue.js | 2.6.10 |
| UI 组件库 | Element UI | 2.15.14 |
| 状态管理 | Vuex | 3.1.0 |
| 路由管理 | Vue Router | 3.0.6 |
| 国际化 | vue-i18n | ^8.28.2 |
| HTTP 客户端 | Axios | 0.18.1 |
| 构建工具 | Vue CLI | 4.4.4 |
| 样式处理 | Sass | 1.26.8 |
| CSS 重置 | Normalize.css | 7.0.0 |
| 图标库 | Font Awesome Free | ^7.0.0 |
| CSS 工具类 | Bootstrap | ^5.3.0 |
| 图表库 | ECharts | ^6.0.0 |
| Excel 导出 | xlsx | ^0.18.5 |
| Cookie | js-cookie | 2.2.0 |
| 进度条 | NProgress | 0.2.0 |
| 代码规范 | ESLint + eslint-plugin-vue | 6.7.2 / 6.2.2 |
| 单元测试 | Jest + @vue/test-utils | - |

> Node 引擎要求 `>=8.9`，npm `>= 3.0.0`。本工程无任何 Mock 依赖与 Mock 服务，开发联调需要真实后端运行。

## 环境要求与快速开始

```bash
# 安装依赖
npm install

# 启动开发服务（默认端口 9528）
npm run dev

# 指定端口
port=8080 npm run dev
```

启动后访问 `http://localhost:9528`。默认登录页 `/login`，登录接口为后端 `POST /public/user/login`，需要后端服务（默认 `http://127.0.0.1:8080`，见 [开发代理](#开发代理与后端联调)）。

## 构建与测试命令

| 命令 | 说明 |
|------|------|
| `npm run dev` | 开发服务器（vue-cli-service serve，端口 9528 可覆盖） |
| `npm run build:prod` | 生产构建（输出 `dist/`） |
| `npm run build:stage` | 预发布构建（`--mode staging`） |
| `npm run preview` | 构建产物本地预览（node build/index.js --preview） |
| `npm run lint` | ESLint 检查 `src` 下 `.js`/`.vue` |
| `npm run test:unit` | 单元测试（jest，先清缓存） |
| `npm run test:ci` | lint + 单元测试 |
| `npm run svgo` | 优化 `src/icons/svg` 下的 SVG 图标 |

构建优化（`vue.config.js`）：`chunk-libs`（node_modules 初始依赖）、`chunk-elementUI`（Element UI 独立分包）、`chunk-commons`（公共组件）三级 splitChunks + `runtimeChunk: single`；SVG 图标经 `svg-sprite-loader` 合成雪碧图（symbolId：`icon-[name]`）；生产关闭 sourcemap。

## 核心机制

### 动态路由加载

前端路由 = 静态路由（`constantRoutes`：`/login`、`/404`、`/403`、`/` 欢迎页）+ 后端下发的动态路由。

```
main.js → bootstrap()
  → getServiceRouter()                          异步生成路由
    → generateRoutesFromServer()
      → 合并 constantRoutes
      → 有 Token 时：
          routeCacheService.getCachedRoutes()   命中 localStorage 缓存（15 分钟过期）直接用
          │  未命中 → GET /private/view/routes   拉取动态路由并缓存
          │           → GET getGatewayVersion() 记录服务端视图版本号（失败不阻断）
          → mapComponent()                       组件字段映射
              'Layout' → layout 组件
              其他    → require.context('@/views') 按 key 匹配 .vue
                        未命中 → fallback 到 404 页面
      → 追加 { path: '*', redirect: '/404' }
    → new Router() → setUpRouterPermissions()   注册导航守卫
  → new Vue({ router, store, i18n }).$mount('#app')
```

路由版本管理 API（对接后端 `dynamic-view-route` 模块的 append-only 版本机制）：

| API | 后端路径 | 说明 |
| --- | --- | --- |
| `getRoutes()` | `GET /private/view/routes` | 拉取当前分组动态路由 |
| `saveRouteConfigs(group, configs, description)` | `POST /private/view/routes/{group}` | 保存路由（产生新版本） |
| `deleteRouteConfigs(group)` | `DELETE /private/view/routes/{group}` | 删除分组 |
| `getRouteGroups()` / `getRouteConfigsByGroup(group)` | `GET .../route-groups` / `GET .../routes/{group}` | 分组列表/分组配置 |
| `getRouteVersions(group)` | `GET .../routes/{group}/versions` | 版本历史（最新在前） |
| `rollbackRouteVersion(group, versionId)` | `POST .../routes/{group}/rollback/{versionId}` | 回滚（复制产生新版本） |
| `refreshRouteCache()` | `POST .../routes/refresh` | 服务端缓存刷新 |

`router/index.js` 还导出 `updateRouter()`（并发锁 + 等待队列，matcher 热替换）、`refreshRoutes()`（清缓存后重拉）、`resetRouter()`；`main.js` 将 Vue 实例挂到 `window.vueInstance` 供路由热更新使用。

### 导航守卫（permission.js）

- 白名单仅 `/login`；无 Token 一律重定向登录页（带 `redirect` 回跳参数）；
- 有 Token：未获取用户信息时 `user/getInfo` → 首次或登录跳转后 `updateRouter()` 热加载动态路由（`dynamicRoutesLoaded` 防重复加载）；
- `getInfo` 失败 → `user/resetToken` 清 Token 回登录页；
- `meta.roles` 非空时经 `hasRole(userRoles, to.meta.roles)` 校验，不通过跳 `/403`；
- 路由更新进行中会 `waitForRouteUpdate()` 等待完成再放行；
- NProgress 进度条 + `getPageTitle` 动态页面标题。

### 用户状态（store/modules/user.js）

state：`token`、`refreshToken`、`name`（昵称）、`userName`、`avatar`、`roles`（权限项展平列表）、`roleGroups`（角色组，含 permissions）、`departments`、`currentDepartment`、`routes`、`accountStatus/accountStatusStr`。

关键 actions：

- `login`：`POST /public/user/login` → 存 Token（localStorage，键 `SYSTEM_TOKEN`）+ 记住我状态；
- `getInfo`：拉取用户信息，把后端角色组 `avaliableRoles[].permissions` 展平为 `roles`；
- `switchDepartment`：`POST /private/user/setCurrentDepartment/{id}` 切换当前部门后刷新用户信息；
- `refreshToken`：`GET public/user/refresh`（10 秒 AbortController 超时），成功更新 Token；
- `logout`：调登出接口（失败不阻断）→ 清 Token → `resetRouter` + 重置动态路由标记 + 清路由缓存。

## 请求封装与令牌刷新

`src/utils/request.js` 封装 Axios 实例（`baseURL = process.env.VUE_APP_BASE_API`，`withCredentials: true`）：

**请求拦截器**：携带 Token 请求头（`settings.tokenHeader`，默认 `Authorization`）+ `Accept-Language`（`i18n.locale === 'en' ? 'en_US' : 'zh_CN'`）。

**响应拦截器**按业务码路由（码值与后端 web 模块 `AjaxResult` 约定一致）：

| 业务码 | 处理 |
| --- | --- |
| `200`（successCode） | 成功返回；响应含 `message + type` 时弹 Message |
| `405`（noLoginCode） | **先尝试刷新 Token**（`user/refreshToken`）：刷新期间并发请求进入 `failedQueue` 排队，刷新成功后统一重发（含 baseURL 重复前缀修复）；刷新失败转 405 流程 |
| `468`（refreshTokenCode） | 弹确认框（"会话已过期"）→ `resetToken` → 整页 reload 重新登录 |
| `601`（debugCodes） | 仅 console.log |
| `701`（noticeCodes） | Notification 通知（title/type/duration 取响应字段） |
| 其他 | Message 错误提示 |

额外能力：响应含 `redirect` 字段时弹通知提示并延时 2 秒后 `$router.push(res.redirect)` 跳转；无 `code` 的响应按配置错误拒绝。

**Token 存储（utils/auth.js）**：localStorage（键 `settings.tokenCookieKey` = `SYSTEM_TOKEN`）。「记住我」状态下 `setToken(token, true)` 会额外调用 `GET /private/user/getRefreshToken` 获取刷新令牌，供无感续期使用；记住我状态存 `REMEMBER_ME` 键。

## 国际化

- `src/i18n/`：`zh.js` / `en.js` 语言包 + Element UI 中英文 locale 合并；`vue-i18n` 实例语言取 cookie `language`（默认 `zh`）；
- `main.js` 中 Element UI 经 `i18n: (key, value) => i18n.t(key, value)` 接入；
- 请求自动携带 `Accept-Language`（`zh_CN` / `en_US`，与后端 web 模块 `I18nFilter` 的解析约定匹配）；
- 顶栏 `LangSelect` 组件切换语言。

## API 模块清单

`src/api/` 共 18 个模块（`utils/request.js` 统一封装）：

| 模块 | 对接后端能力 |
|------|------|
| `user.js` | 登录/登出/用户信息/刷新令牌/切换部门/状态枚举 |
| `admin_user.js` | 管理员用户 CRUD、角色分配、登录会话查看、Token 禁用 |
| `role.js` / `role_group.js` | 角色与角色组 CRUD、启用禁用 |
| `company.js` / `department.js` | 公司/部门 CRUD 与树形层级移动 |
| `view.js` | 表单配置 CRUD + 动态路由分组/版本/回滚（见上文） |
| `dic.js` | 数据字典（对接 `dic` 微服务经 Dubbo 暴露的字典能力） |
| `system.js` | 系统级接口（含 `getGatewayVersion` 全局视图版本号） |
| `systemlog.js` | 操作日志统计报表（对接 `logging` 模块 `ISystemLogService`） |
| `app-version.js` | 应用版本管理（对接 `system` 模块版本服务） |
| `cache.js` | 缓存管理（对接 `cache` 模块） |
| `oss.js` | 对象存储（对接 `oss` 模块） |
| `generic-api.js` | 泛化 API 调试（对接 `web` 模块 genric 调用入口） |
| `trace.js` | 链路追踪查询 |
| `table.js` | 表格演示数据 |
| `eventSubscription.js` | 事件上报器/事件/触发器元数据、订阅 CRUD 与启停、`POST report` 模拟上报（对接 `private/event-subscription/*`） |
| `eventAction.js` | 动作元数据、动作绑定 CRUD 与启停、`POST test` 单动作测试（对接 `private/event-action/*`） |

## 事件编排页面

`/event-subscription`（事件订阅）与 `/event-action`（事件动作）是开发者控制台中的两个静态路由，与 `/condition-rule` 同属"事件上报 → 订阅绑定 → 条件触发 → 业务执行"链路：条件规则继续在现有 `ConditionRuleManager.vue` 编排，订阅页通过 `?ruleCode=` 可直接跳转定位。

两个页面均采用「左侧绑定列表 + 右侧编排」双栏结构（平板收窄、手机单列）：

- **事件订阅页**：选择上报器 → 事件 → 触发器 → 条件规则 → 顺序与启停；`save-event` 按事件整体提交订阅列表；底部调试区按事件参数声明渲染模拟载荷，`report` 后展示上报结果与触发器明细（已触发/未触发/调用异常）。
- **事件动作页**：选择上报器 → 事件 → 动作 → 动态参数表单；动作参数由后端 `ActionMeta.paramTypes` 驱动，复用 `src/utils/valueGetter.js` 的 `toParamFields/buildFormData/validateFields/toParamValues` 与公共组件 `GetterParamField`；支持 `${paramName}` 占位符引用事件参数，`test` 可单动作试跑。

`src/views/dev/event/` 下的子组件：

| 组件 | 职责 |
|------|------|
| `EventScopePicker.vue` | 上报器/事件两级下拉与事件参数只读标签；内部用 `reporterOptions` / `eventOptions` 兼容后端 `id|reporterId`、`code|eventCode` 两种字段命名 |
| `SubscriptionRows.vue` | 订阅行编辑器：触发器、条件规则、顺序、启停、删除与重复校验 |
| `ActionBindingPanel.vue` | 动作行编辑器：动作选择、参数表单回填与收集、顺序/启停 |
| `EventParamForm.vue` | 基于 `ParamType` 的动态参数表单，暴露 `reset` / `setValues` / `validate` / `buildParamValues` |
| `EventReportPanel.vue` | 模拟上报与触发器明细结果展示 |
| `ActionTestPanel.vue` | 单动作测试执行与回执展示 |

`src/utils/eventMeta.js` 统一解包 `data` / `data.data` / `list` / `records` 等响应包装并对齐字段名，页面只依赖规范化后的 `reporterId` / `code` / `triggerId` / `actionId` 等字段；元数据接口失败时以 `console.error` 输出，便于定位 403（权限脚本未执行）等问题。文案位于 `src/i18n/zh.js` / `en.js` 的 `eventCommon` / `eventSubscription` / `eventAction` 命名空间。

## 项目结构

```
app-ui-vue/
├── public/                       # 静态 HTML 模板
├── src/
│   ├── api/                      # 18 个后端 API 模块（见上表）
│   ├── assets/                   # 静态资源
│   ├── components/               # 21 个公共组件目录
│   │   ├── DynamicForm/          #   动态表单渲染（JSON 配置 → 表单）
│   │   ├── FormEditor/           #   表单可视化编辑器（含字段配置子组件）
│   │   ├── DynamicTable/         #   动态表格
│   │   ├── DynamicOptions/       #   API 动态选项下拉
│   │   ├── CardList/             #   卡片列表
│   │   ├── Chart/                #   ECharts 图表封装
│   │   ├── UserAdminView/        #   用户管理视图
│   │   ├── UserManagementView/   #   用户管理视图（新版）
│   │   ├── RoleAdminView/        #   角色管理视图
│   │   ├── RoleGroupAdminView/   #   角色组管理视图
│   │   ├── CompanyAdminView/     #   公司管理视图
│   │   ├── DataDicAdminView/     #   数据字典管理视图
│   │   ├── DepartmentTreeView/   #   部门树视图
│   │   ├── QuickUseManagementView/ # 快捷管理视图
│   │   ├── PermissionControlPanel/ # 权限控制面板
│   │   ├── LangSelect/           #   语言切换
│   │   ├── UrlImage/             #   URL 图片预览/配置
│   │   ├── UserAvatar/           #   用户头像
│   │   ├── Breadcrumb/          #   面包屑导航
│   │   ├── Hamburger/           #   侧边栏折叠按钮
│   │   └── SvgIcon/             #   SVG 图标
│   ├── icons/                    # SVG 图标源（svg-sprite-loader 雪碧图）
│   ├── i18n/                     # 中英文语言包（zh.js / en.js / index.js）
│   ├── layout/                   # 主布局（Sidebar + Navbar + AppMain + ResizeHandler）
│   ├── router/                   # 路由（静态 + 动态加载 + 热更新 + 缓存）
│   ├── store/                    # Vuex（app/settings/user/FormConfig/routes 模块）
│   ├── styles/                   # 全局样式（MacOS AppStore 风格侧边栏主题）
│   ├── utils/                    # 工具（request/auth/route-cache/permission/excel/validate 等）
│   │   └── eventMeta.js          #   事件编排元数据解包与字段归一化（见上文）
│   ├── views/                    # 页面
│   │   ├── dev/                  #   开发者控制台（见「事件编排页面」）
│   │   │   ├── EventSubscriptionManager.vue   #   事件订阅编排
│   │   │   ├── EventActionManager.vue         #   事件动作编排
│   │   │   ├── ConditionRuleManager.vue       #   条件规则编排（支持 ?ruleCode= 直达）
│   │   │   ├── ValueGetterTester.vue          #   值获取器调试
│   │   │   └── event/                         #   事件编排公共子组件（6 个，见上文）
│   │   ├── login/                #   登录页
│   │   ├── dashboard/            #   欢迎页
│   │   ├── admin/                #   管理页面
│   │   │   ├── user/             #     用户管理
│   │   │   ├── role/             #     角色管理
│   │   │   ├── role_group/       #     角色组管理
│   │   │   ├── company/          #     公司管理
│   │   │   ├── dic/              #     数据字典管理
│   │   │   ├── systemlog/        #     系统日志
│   │   │   ├── version/          #     应用版本管理
│   │   │   ├── generic-api/      #     泛化 API 调试
│   │   │   └── route-manager/    #     路由配置管理（版本/回滚）
│   │   ├── cache/                #   缓存管理
│   │   ├── oss/                  #   对象存储管理
│   │   ├── user/                 #   用户相关页面
│   │   ├── form/                 #   动态表单演示/创建/管理
│   │   ├── 403.vue / 404.vue     #   错误页
│   ├── App.vue / main.js / permission.js / settings.js
├── tests/unit/                   # 单元测试（组件 + 工具函数）
├── .env.development              # VUE_APP_BASE_API=/api
├── .env.staging                  # VUE_APP_BASE_API=/stage-api
├── .env.production               # VUE_APP_BASE_API=/api
├── vue.config.js                 # 代理/分包/SVG Sprite（见下）
├── nginx.conf                    # 生产部署参考配置
├── debug-routes.js               # 浏览器控制台路由调试脚本
├── DEPLOYMENT.md                 # 部署指南
└── jest.config.js / jsconfig.json / babel.config.js / postcss.config.js
```

## 配置说明

### 全局配置（src/settings.js）

```js
module.exports = {
  title: '小童快速开发模板',
  fixedHeader: false,
  sidebarLogo: false,
  backend: 'http://127.0.0.1:8080',   // 开发代理目标（后端 app 服务地址）
  tokenHeader: 'Authorization',       // Token 请求头
  tokenCookieKey: 'SYSTEM_TOKEN',     // Token 在 localStorage 的键名
  successCode: 200,                   // 成功码
  noLoginCode: [405],                 // 需刷新 Token 的错误码
  refreshTokenCode: [468],            // 刷新失败需重新登录的错误码
  debugCodes: [601],                  // 调试信息码
  noticeCodes: [701],                 // 通知信息码
  // 动态表单配置映射 key（表单配置存于后端 view 模块，按 key 取）
  userSearchFormKey: '1', userAddFormKey: '2', userEditFormKey: '3',
  roleSearchFormKey: '6', roleAddFormKey: '12', roleEditFormKey: '13',
  roleGroupSearchFormKey: '14', roleGroupAddFormKey: '15', roleGroupEditFormKey: '15'
}
```

### 环境变量

| 文件 | 用途 | `VUE_APP_BASE_API` |
|------|------|---------------------|
| `.env.development` | 开发 | `/api` |
| `.env.staging` | 预发布 | `/stage-api` |
| `.env.production` | 生产 | `/api` |

> 生产构建的 base api 也是 `/api`，因此生产部署时 Nginx 必须配置 `/api` 反向代理到后端（见部署节）。

## 开发代理与后端联调

`vue.config.js` 的 devServer 将 `/api` 代理到 `src/settings.js` 的 `backend`（默认 `http://127.0.0.1:8080`），并重写路径去掉 `/api` 前缀：

```js
devServer: {
  port: 9528,                      // 可用 port=xxxx npm run dev 覆盖
  proxy: {
    '/api': {
      target: 'http://127.0.0.1:8080',
      changeOrigin: true,
      pathRewrite: { '^/api': '' }
    }
  }
}
```

后端地址变更时改 `backend` 字段即可，无需改环境变量。

## 部署

详细步骤另见 `DEPLOYMENT.md`。要点：

1. **构建**：`npm run build:prod` → 产物在 `dist/`（静态资源在 `static/` 子目录）；
2. **Nginx**：仓库自带 `nginx.conf` 参考配置（监听 **8866**）：
   - `location /` 指向 `dist` 目录，`try_files $uri $uri/ /index.html` 支持 history 模式；
   - `location /api/` **先 rewrite 去掉 `/api` 前缀**再 `proxy_pass http://127.0.0.1:8082`（即后端 app 服务实际端口）；
   - 静态资源 7 天缓存、gzip、WebSocket 升级头、300s 超时、隐藏文件拒绝访问、404 回落 index.html；
   - 配置内的 root/日志路径为 macOS 绝对路径，跨平台部署需按注释调整；
3. **Docker**：`FROM nginx:alpine` + `COPY dist /usr/share/nginx/html` + `COPY nginx.conf /etc/nginx/conf.d/default.conf`；
4. **后端地址变更**：改 `nginx.conf` 的 `proxy_pass`；开发环境改 `settings.js` 的 `backend`。

## 调试工具

- `debug-routes.js`：粘贴到浏览器控制台运行的脚本，检查 `window.$router`/store/当前路由/全部可用视图组件；
- 路由加载链路在 `router/index.js` 内有大量 `console.log` 调试日志（组件映射、缓存命中、更新锁等），生产构建仍会输出；
- 601 调试码响应会在控制台打印（见请求拦截器）。

## 注意事项

1. **无 Mock 能力**：工程不含 MockJS 等依赖，开发需要真实后端（app 模块 + Nacos/Redis/MySQL 等依赖服务）在 `backend` 地址运行。
2. **路由缓存 15 分钟**：动态路由缓存于 localStorage（`dynamic_routes_cache`），过期自动清除；另有独立视图版本号键 `dynamic_routes_version`（仅在实际拉取路由后更新，`clearCache` 不清除）。强制刷新用 `refreshRoutes()` 或页面 `route-manager` 的版本管理功能。
3. **Token 在 localStorage**：XSS 场景下可被脚本读取，属权衡后的实现（代码注释保留了 Cookie 方案的痕迹但当前只读写 localStorage）。
4. **405/468 语义**：405 = Token 失效先静默刷新重试（并发排队），468 = 刷新失败确认弹窗整页重登；与后端 web 模块 `AjaxResult` 的 `RELOGIN_CODE(405)`/`REFRESH_FAIL_CODE(468)` 呼应。
5. **`require.context` 全量预载 views**：动态路由组件按 `.${component}` key 匹配，视图文件名与后端路由配置的 `component` 字段必须严格对应，否则 fallback 404。
6. **生产 base api 为 `/api`**：与 staging 的 `/stage-api` 不同，生产 Nginx 代理路径须与之一致。
7. **axios 0.18.1 较老**：`AbortController` 超时控制依赖较新浏览器环境。
8. **`window.vueInstance`**：路由热更新依赖全局实例引用，生产代码如此设计。
9. **Element UI 主题覆盖**：样式在 `src/styles/element-ui.scss`；侧边栏为 MacOS 风格主题（`variables.scss`：背景 `#f0f0f0`、激活 `#0071e3`、圆角 12px、宽 220px）。
10. **构建产物已入库**：`dist/` 目录当前被提交进仓库，按需清理或加入 `.gitignore`。

## 许可证

MIT License（见 `LICENSE`）。
