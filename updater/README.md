# Updater - 应用自动更新（类库 + GUI 演示）

> 本文件同时覆盖 **`updater`（可嵌入类库）** 与 **`updater-gui`（Swing 图形化更新器演示程序）** 两个模块。

## 概述

| 模块 | artifactId | 定位 | 产物 |
|------|------------|------|------|
| `updater` | `updater` | 可嵌入的 Java 类库，**无 main 方法** | 普通 JAR（仅依赖 Jackson） |
| `updater-gui` | `updater-gui` | 基于类库的 Swing 演示程序，含 `main` | 可执行 fat JAR（`jar-with-dependencies`） |

更新流程：

1. **检查更新**：POST 调用检查更新接口，提交 `appKey`、`platform`、`currentVersionCode`；
2. **下载更新包**：支持**多文件**下载，每个文件可配多个镜像地址容灾，带进度与速度回调；
3. **执行部署脚本**（可选）：全部下载文件的绝对路径依次作为参数传入，脚本**后台启动、不等待结束**；
4. **版本持久化**：更新成功后将新版本号写入 `.installed-version` 文件，供下次启动比对。

依赖面：JDK 11+ 标准库 + Jackson（`jackson-databind`，版本由父 POM 管控），HTTP 走 JDK 自带 `HttpURLConnection`，无其他第三方依赖。

## 模块结构

```
updater/
├── src/main/java/com/tbs/updater/
│   ├── config/      # UpdaterConfig（Builder + properties 加载 + 版本文件读写）
│   ├── model/       # UpdateInfo / DownloadFile
│   ├── exception/   # UpdateException（6 种 ErrorType）
│   ├── api/         # AutoUpdater（门面）/ UpdateListener（回调）
│   └── impl/        # UpdateChecker / Downloader / ScriptExecutor
└── src/main/resources/
    ├── deploy.sh / deploy.bat / deploy.cmd     # 部署脚本模板
    └── updater.properties.template             # 配置文件模板

updater-gui/
└── src/main/java/com/tbs/updater/demo/UpdaterGuiDemo.java   # Swing GUI（唯一源文件）
```

## 快速开始（类库）

### Maven 引入

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>updater</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 一键自动更新

```java
UpdaterConfig config = new UpdaterConfig.Builder()
        .serverHost("192.168.10.219")
        .serverPort(8080)
        .appKey("兴昊生产扫码入库App")
        .platform("android")
        .currentVersionCode(0)
        .downloadDir("/tmp/updates")
        .deployScript("/opt/updater/deploy.sh")
        .build();

AutoUpdater updater = new AutoUpdater(config, new UpdateListener() {
    @Override
    public void onCheckComplete(UpdateInfo info) {
        if (info.isNeedUpdate()) {
            System.out.println("发现新版本: " + info.getLatestVersionName()
                    + "（动作: " + info.getAction() + "）");
        } else {
            System.out.println("当前已是最新版本");
        }
    }

    @Override
    public void onDownloadProgressDetail(long downloadedBytes, long totalBytes, int percent) {
        System.out.printf("\r下载 %d%%  %d/%d bytes%n", percent, downloadedBytes, totalBytes);
    }

    @Override
    public void onSuccess() { System.out.println("更新流程完成"); }

    @Override
    public void onError(String errMsg) { System.err.println("更新失败: " + errMsg); }
});

updater.autoUpdate();   // 一键：检查 → 下载全部文件 → 部署 → 版本回写
```

`UpdateListener` 全部方法为 `default` 空实现，按需覆盖即可。

### 分步控制（GUI / 两段式确认场景）

```java
new Thread(() -> {
    try {
        // 阶段一：只检查，不下载
        UpdateInfo info = updater.checkForUpdate();
        if (!info.isNeedUpdate()) {
            return;
        }
        // 阶段二：用户确认后安装（下载 → 部署 → 版本回写）
        updater.installUpdate(info);
    } catch (UpdateException e) {
        // e.getErrorType() 区分错误类别，e.getMessage() 获取描述
    }
}).start();
```

也可完全手动编排：

```java
List<File> files = updater.downloadAllUpdates(info);   // 多文件下载
updater.executeDeployScript(files);                    // 传全部文件路径给脚本
```

> **已废弃方法**：`downloadUpdate(UpdateInfo)`（返回单 File）与 `executeDeployScript(File)` 均已标注 `@Deprecated`，请使用 `downloadAllUpdates(UpdateInfo)` 与 `executeDeployScript(List<File>)`。

## 配置说明

### Builder 配置项

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `serverHost` | String | 无（必填） | 服务端主机，可携带 `http://`/`https://`，缺省补 `http://` |
| `serverPort` | int | 0 | ≤0 时 URL 不带端口 |
| `checkPath` | String | `/public/app-update/check` | 检查更新接口路径 |
| `appKey` | String | 无（必填） | 应用唯一标识 |
| `platform` | String | 无（必填） | 平台标识（如 `android`、`ios`） |
| `currentVersionCode` | int | 0 | 当前应用版本号 |
| `downloadDir` | String | `java.io.tmpdir` 下 `updates` | 下载目录 |
| `deployScript` | String | null（跳过部署） | 部署脚本路径 |
| `timeoutSeconds` | int | 30 | 检查更新的连接/读取超时（秒） |
| `tryAllUrls` | boolean | true | 下载失败时是否依次尝试其余镜像地址 |

`validate()` 校验三项必填：`serverHost` / `appKey` / `platform`。

### properties 文件配置

`UpdaterConfig.fromFile(path)` 按 UTF-8 读取（支持中文值），`fromProperties(props)` 接收 `Properties` 对象。键名常量见 `UpdaterConfig.KEY_*`：

```properties
server.host=192.168.10.219
server.port=8080
server.checkPath=/public/app-update/check
app.appKey=兴昊生产扫码入库App
app.platform=android
app.currentVersionCode=0
local.downloadDir=/tmp/updates
local.deployScript=/opt/updater/deploy.sh
update.timeoutSeconds=30
update.tryAllUrls=true
```

```java
UpdaterConfig config = UpdaterConfig.fromFile("/etc/updater/updater.properties");
AutoUpdater updater = new AutoUpdater(config, listener);
```

> **模板文件注意**：`updater.properties.template` 中的 `local.startScript`（启动脚本）在 `UpdaterConfig` 中**无对应键名常量**，会被解析忽略——当前版本不支持"部署脚本 + 启动脚本"两个脚本，仅 `deployScript` 生效。

### 本地版本文件

| 项 | 值 / 方法 |
|----|-----------|
| 文件名 | `.installed-version`（内容为最新 versionCode 字符串） |
| 默认目录 | JVM 工作目录（`user.dir`） |
| 自定义目录 | `UpdaterConfig.setLocalVersionDir(String)`（置 null 恢复默认） |
| 读取 | `UpdaterConfig.readLocalVersion()`，无记录或文件损坏返回 `-1` |
| 写入 | `AutoUpdater` 在部署脚本启动后自动回写，写入失败仅回调 `onError` 不中断流程 |

GUI 启动时会将该目录锚定到 **JAR 包所在目录**（`setLocalVersionDir(resolveJarDir())`），避免随工作目录漂移。

## 接口契约

**请求**：`POST {serverHost}:{serverPort}/public/app-update/check`

```json
{ "appKey": "xxx", "platform": "android", "currentVersionCode": 0 }
```

**响应**（`code` 固定 200 为成功，其余值携带 `message` 抛 `SERVER_ERROR`）：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "needUpdate": true,
    "action": "FORCE",
    "latestVersionCode": "102",
    "latestVersionName": "v1.0.2",
    "releaseNote": "修复若干问题",
    "files": [
      { "name": "app.jar", "downloadUrls": ["https://a/app.jar", "https://b/app.jar"] }
    ]
  }
}
```

`UpdateInfo.parseFromJson` 解析规则：

- `latestVersionCode` 兼容字符串与数字两种下发形态，统一按 **字符串** 持有；
- **新格式**：`files` 数组优先。每个元素支持对象（`{name, downloadUrls}`）或纯字符串（视为 URL，文件名从 URL 提取）；元素内 `downloadUrls` 数组或 `downloadUrl` 单字符串均可；
- **旧格式兼容**：顶层 `downloadUrls` 数组或 `downloadUrl` 单字符串 → 自动包装为单个 `DownloadFile`，文件名退化为 `{appKey}-{versionCode}`；
- 空格项被过滤，`downloadUrls` 为空的文件项被丢弃。

本地文件名规则（`AutoUpdater.downloadAllUpdates`）：服务端指定 `name` 则用之；否则多文件用 `{appKey}-{versionCode}({index})`、单文件用 `{appKey}-{versionCode}`；扩展名始终从 URL 提取。

## 核心组件

| 组件 | 职责 |
|------|------|
| `UpdaterConfig` | 不可变配置类：Builder 构建 / properties 加载 / `buildCheckUrl()` 拼地址 / `validate()` 校验 / 本地版本文件读写 |
| `UpdateInfo` | 更新信息：`needUpdate`、`action`、`latestVersionCode`、`latestVersionName`、`releaseNote`、`files`、`downloadUrls` |
| `DownloadFile` | 单文件描述：`name` + `downloadUrls`（多镜像） |
| `UpdateException` | 统一异常，`ErrorType`：`CONFIG_ERROR` / `NETWORK_ERROR` / `SERVER_ERROR` / `PARSE_ERROR` / `DOWNLOAD_ERROR` / `SCRIPT_ERROR` |
| `UpdateListener` | 12 个回调节点，全部 default 空实现 |
| `AutoUpdater` | 门面：`autoUpdate()` / `installUpdate(info)` / `checkForUpdate()` / `downloadAllUpdates(info)` / `executeDeployScript(List)` |
| `UpdateChecker` | POST 检查接口，校验 HTTP 200 与业务 code 200 |
| `Downloader` | 流式下载、跨协议重定向跟随（≤5 跳）、进度与字节回调、失败清理不完整文件 |
| `ScriptExecutor` | `execute`（等待，默认 120s 超时强杀）/ `executeAsync`（只启动不等待） |

### UpdateListener 回调全景

| 回调 | 时机 |
|------|------|
| `onCheckStart` | 检查开始 |
| `onCheckComplete(info)` | 检查完成 |
| `onDownloadFileStart(fileIndex, totalFiles, url)` | 每个文件开始下载（多文件场景） |
| `onDownloadStart(url)` | 开始尝试某个地址 |
| `onDownloadProgress(percent)` | 进度百分比变化 |
| `onDownloadProgressDetail(downloadedBytes, totalBytes, percent)` | 字节级进度（`totalBytes=-1` 表示未知） |
| `onDownloadComplete(file)` | 单个文件下载完成 |
| `onAllDownloadsComplete(files)` | 全部文件下载完成 |
| `onDeployStart` | 部署脚本即将后台启动 |
| `onDeployComplete` | 脚本已启动（**不代表执行完毕**） |
| `onSuccess` | 全流程成功（含"已是最新版"的直接成功） |
| `onError(errMsg)` | 流程失败 |

### 下载与脚本行为细节

**Downloader**：连接 30s / 读取 60s 超时；8KB 缓冲；同协议重定向由 `HttpURLConnection` 自跟随，跨协议（http→https）手动解析 `Location` 逐级跳转；仅在进度百分比变化时回调（减少抖动）；**下载中断会删除不完整文件**（删除失败则 `deleteOnExit`）。

**ScriptExecutor**：按脚本扩展名选择执行方式——`.sh`/`.bash` 显式用 `bash`（避免 macOS 参数丢失）、`.bat`/`.cmd` 用 `cmd /c`（确保参数传递）、其他直接执行；工作目录为**脚本所在目录**；stderr 合并进 stdout 由守护线程实时透传（前缀 `[script] `），输出解码用**系统默认字符集**（Windows GBK / Linux-macOS UTF-8）。

## GUI 演示程序（updater-gui）

### 构建与运行

```bash
# 打包（仓库根目录）
mvn clean package -pl updater-gui -am

# 运行 —— Windows 必须加 -Dfile.encoding=UTF-8 避免中文乱码
java -Dfile.encoding=UTF-8 -jar updater-gui/target/updater-gui-1.0-SNAPSHOT-jar-with-dependencies.jar

# Linux/macOS 可直接运行
java -jar updater-gui/target/updater-gui-1.0-SNAPSHOT-jar-with-dependencies.jar
```

`Main-Class` 为 `com.tbs.updater.demo.UpdaterGuiDemo`（由 assembly 插件 archive 配置写入，不依赖 MANIFEST.MF 资源文件）。父 POM 继承的 Spring Boot / Dubbo / dotenv 等依赖在 GUI 模块中统一置为 `provided`，不进入 fat JAR。

> 旧版文档中的"`mvn -pl updater clean compile` + `java -cp ... UpdaterGuiDemo`"运行方式**已失效**——GUI 已拆分为独立模块，不再位于 `updater` 内。

### 功能与交互

启动后**必须先选择应用配置**（`updater-{appKey}.properties`，位于 **JAR 同目录**）：

| 功能 | 说明 |
|------|------|
| 应用选择 | 列出全部 `updater-{appKey}.properties`；支持"打开 / 新建 / 删除 / 退出" |
| 新建向导 | 填写主机、端口、appKey、平台、版本号；支持先"测试连接"再保存；同 appKey 已存在时询问覆盖；appKey 禁止含路径分隔符 |
| 切换应用 | 运行时切换到其他 appKey，配置与版本号各自独立 |
| 两段式更新 | 主按钮先"检查"，发现新版本后弹确认框（展示版本、更新日志、升级动作）；同意才进入下载安装 |
| 进度显示 | 进度条 + 百分比 + 实时速度（`formatSpeed`）+ 已下载/总量（`formatFileSize`） |
| 结果确认 | 部署脚本启动后弹"已成功 / 未成功"：成功则把新版本号**回写到当前 appKey 的配置文件**；未成功保持原版本，强制更新时要求重试 |
| 配置编辑 | 运行时编辑全部配置项（主机/端口/appKey/平台/版本/下载目录/部署脚本/超时/多地址尝试），保存即写回对应配置文件 |
| 自动打开 | 复选框控制下载完成后是否自动打开文件（`Desktop.open`） |
| 操作日志 | 带 `HH:mm:ss` 时间戳的日志区 |
| 单实例保护 | 基于 JAR 目录下的 `.updater.lock` 文件锁（`FileChannel.tryLock`），重复启动弹出提示；锁机制异常时不阻止启动 |
| 关窗拦截 | 更新中关闭窗口需二次确认 |

强制更新判定：`isForceUpdate(UpdateInfo)` 依据服务端下发的 `action` 字段识别，强制更新时不可跳过。

界面配色：空闲灰 `#646464`、进行中蓝 `#2196F3`、成功绿 `#4CAF50`、失败红 `#F44336`；字体 `Microsoft YaHei`。

## 部署脚本

`updater/src/main/resources/` 提供三份等价模板（`deploy.sh` / `deploy.bat` / `deploy.cmd`），用法一致：

```
deploy.sh <主JAR包路径> [附加文件1] [附加文件2] ...
```

`deploy.sh` 行为：打印全部参数 → 校验主 JAR 存在 → `lsof -ti :${APP_PORT}`（默认 8080）查找旧进程并 `kill`（等待最多 10 秒，未退出则 `kill -9`）→ `nohup java -jar "$JAR_FILE" > "${JAR_DIR}/app.log" 2>&1 &` 后台启动新服务。附加文件（`$2`…）仅打印路径，复制/解压逻辑由使用者在脚本中自定义（模板内已给出注释示例）。

使用时按需修改脚本中的 `APP_PORT` 与部署逻辑，并通过 `local.deployScript` 指向其绝对路径。

## 最佳实践

1. **后台线程执行**：`autoUpdate()` 与分步方法均为阻塞调用；GUI 场景务必放到后台线程，回调中更新 UI 需切换回 UI 线程（`SwingUtilities.invokeLater`）
2. **部署脚本后台执行**：`AutoUpdater` 用 `executeAsync` 启动脚本且不等待；脚本应自行守护化（nohup / start / systemd）后立即返回，部署结果由用户确认
3. **下载目录预检**：确保运行用户对 `downloadDir` 有写权限；下载失败时类库自动清理不完整文件
4. **版本目录锚定**：GUI 已锚定到 JAR 目录；自研集成方建议调用 `UpdaterConfig.setLocalVersionDir()` 锚定到固定目录，避免受启动工作目录影响
5. **可测试性**：`AutoUpdater` 提供五参构造函数，可注入自定义的 `UpdateChecker` / `Downloader` / `ScriptExecutor` 子类或 mock
6. **线程安全**：类库不持有共享可变状态，单线程顺序调用 `autoUpdate()`；并发多流程由调用方同步
7. **Windows 编码**：GUI 与含中文的配置在 Windows 上需 `-Dfile.encoding=UTF-8`；脚本输出解码用系统默认字符集，属有意设计

## 故障排除

| 现象 | 错误类型 | 排查方向 |
|------|----------|----------|
| 缺少必填配置项 | CONFIG_ERROR | `serverHost` / `appKey` / `platform` 是否配置 |
| 检查更新网络失败 | NETWORK_ERROR | 网络连通性、主机端口、超时时间 |
| 检查更新业务错误 | SERVER_ERROR | 服务端返回的 code/message，appKey 是否已登记 |
| 响应 JSON 解析失败 | PARSE_ERROR | 确认返回标准 `{code, message, data}` 结构 |
| 下载失败 | DOWNLOAD_ERROR | 地址签名是否过期、重定向 Location 是否完整、目录写权限 |
| 脚本启动失败 | SCRIPT_ERROR | 脚本是否存在、`chmod +x`、Windows 下 CMD 参数传递 |
| GUI 启动中文乱码 | — | 加 `-Dfile.encoding=UTF-8` 启动参数 |
| GUI 提示"已在运行" | — | 删除 JAR 目录下的 `.updater.lock`（异常退出残留时） |
| 配置项 `local.startScript` 无效 | — | 该键当前无对应实现，启动逻辑请并入 `deployScript` |

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

---

**最后更新**: 2026年9月
**版本**: 1.0-SNAPSHOT
