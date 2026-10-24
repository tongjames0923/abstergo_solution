# app-ui-flutter（多端应用壳）

基于 `sdk-flutter`（`tbs_client`）构建的 Flutter 客户端基底，覆盖 Web / Windows / macOS / Linux / Android / iOS 六端。首期落地：多授权登录、用户信息与部门切换、SSE 实时推送（连接状态可视化）、消息中心（游标分页/已读/筛选）、应用更新检查（三态提示 + 桌面端下载）、明暗主题与中英文即时切换。UI 采用 forui（shadcn/ui 风格）+ 靛蓝主色现代极简设计，并为管理端整体迁移预留结构（导航模型注入 + 泛化分页协议 + 动态路由/表单配置 client）。

## 架构

```
lib/
├── main.dart / app.dart        入口与根组件（主题/语言装配 + 全局事件监听 + 路由）
├── bootstrap/                  环境解析（--dart-define）+ 日志装配 + 桌面窗口初始化
├── di/                         Riverpod providers：tbsClient/session/SSE 状态/未读数/消息中心/主题/语言
├── routing/                    go_router 声明式路由 + 登录守卫（redirect 恢复被拦截目标页）
├── shell/                      自适应外壳（>=720 宽屏侧栏 / 窄屏底导）+ 顶栏（连接状态/语言/主题）
├── features/
│   ├── auth/                   登录页（login-grants 驱动 + 记住我 + 企微 SSO 跳转）+ OAuth 回调页
│   ├── home/                   工作台（问候/统计卡/消息预览/快捷入口）
│   ├── messages/               消息中心（筛选/无限滚动/批量已读/详情/断连横幅）
│   ├── profile/                个人中心（部门切换/偏好/登出/更新入口）
│   └── update/                 更新服务（三态提示 + 桌面端 .part 原子下载）
├── shared/
│   ├── design/                 设计令牌（靛蓝主色 + 中性底，forui FColors.copyWith 定制）
│   ├── l10n/                   ARB zh/en + flutter gen-l10n
│   └── ...                     通用组件
└── platform/                   平台条件实现（令牌存储：native 安全存储 / Web localStorage；OAuth 启动器）
```

## 多端运行

```bash
cd app-ui-flutter
flutter pub get

# Web（开发调试：跨域直连，依赖后端 WebConfig CORS 放行；生产走同源反代）
flutter run -d chrome --dart-define=TBS_BASE_URL=http://localhost:8080

# Web（生产构建）
flutter build web --release \
  --dart-define=TBS_BASE_URL=/api \
  --dart-define=TBS_APP_KEY=my-app

# 桌面（需本机 Xcode / Visual Studio / GTK 工具链）
flutter run -d macos   --dart-define=TBS_BASE_URL=http://localhost:8080
flutter run -d windows --dart-define=TBS_BASE_URL=http://localhost:8080

# 移动
flutter run -d <device-id> --dart-define=TBS_BASE_URL=http://<局域网IP>:8080
```

| Dart Define | 默认值 | 说明 |
|-------------|--------|------|
| `TBS_BASE_URL` | `http://localhost:8080` | 服务端地址；Web 生产建议 `/api`（同源反代，与 Vue 端 Nginx 模式一致） |
| `TBS_APP_KEY` | `tbs-client` | 升级检查应用标识 |
| `TBS_APP_VERSION_CODE` | `1` | 升级检查内部版本号 |

## 多端差异策略

| 维度 | Web | 桌面 | 移动 |
|------|-----|------|------|
| 会话策略 | `SessionPolicy.web`（405→Cookie 刷新重放，468→重登） | `SessionPolicy.native`（401/405/468→重登） | 同桌面 |
| Token 持久化 | localStorage（shared_preferences） | flutter_secure_storage | flutter_secure_storage |
| SSE 传输 | fetch_client（浏览器 fetch 流，需 HTTP/2 与 Chromium 105+ 支持流式） | package:http 流式（读超时 0） | 同桌面 |
| OAuth | URL 跳转 + 同源回调路由 | 浏览器代跳 | password 授权（企微 deep-link 二期） |
| 更新 | 禁用（Web 无版本概念） | check + .part 原子下载 | check + 打开安装源 |
| 布局 | 侧栏 + 宽屏栅格（承接管理端迁移） | 侧栏 + 窗口管理（window_manager） | 底导 + 单栏卡片流 |

## 后端联调

1. 启动基础设施与主应用（见仓库 `docs/guideline/01-项目总览.md`）：主应用 `http://localhost:8080`。
2. 后端 CORS：`WebConfig.corsFilter` 已放行全部来源/头/方法（`tbs.web.enable-cors` 开关），开发直连可用。
3. **Web 刷新流限制**：当前后端 CORS 未开启 `allowCredentials`，跨域直连时 `withCredentials` 请求会被浏览器拒绝；SDK 已按预设自动降级（native 预设不带凭证）。生产 Web 走同源反代后，`SessionPolicy.web` 的 Cookie 刷新流方可生效。
4. 登录方式由 `GET /public/user/login-grants` 下发；后端未启动时登录页展示缺省账号密码方式并提示加载失败。
5. 企微 SSO 需在 login-grants 的 `publicOptions` 返回 `corpId`/`agentId`，回调地址为 Web 站点 origin（与 Vue 端约束一致）。

## 管理端迁移预留（结构说明）

- **导航注入**：`shell/navigation_model.dart` 的 `AppNavItem` 为外壳唯一数据源，迁移时将 `ViewApi.fetchRoutes()` 的路由树（菜单权限 type=1）映射为导航项列表即可，外壳渲染无需改动。
- **页面映射**：动态路由 `component` 字符串（`'Layout'` 或 views 相对路径）由应用层页面注册表承接。
- **协议底座**：`PagedApi`（`POST {url}?pageNum&pageSize` + `xxxEquals/xxxLike/xxxIn/{field}Order` 条件约定 + `{data,total}` 信封）与 `ViewApi`（动态表单配置 `GET /private/view/form/{name}`）已在 SDK 就绪，管理端 CRUD 页面可直接复用。

## 已知边界

- 语言头 `Accept-Language` 在客户端创建时固化（默认 zh_CN）；运行中切换语言仅影响界面文案，需重启生效于服务端 i18n message。
- 405/468 语义矛盾（规范 vs 前端实现）以 `SessionPolicy` 可配置方式落地，待与后端核对后固化（详见 `sdk-flutter/README.md`）。
- 移动端企微 SSO（deep-link 拉起企微）为二期预留，本期移动端使用账号密码授权。
