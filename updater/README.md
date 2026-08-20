# updater - 应用自动更新类库

## 概述

`updater` 是一个**可嵌入的 Java 类库**（JAR 包），为应用程序提供完整的自动更新流程：

1. **检查更新**：POST 调用检查更新接口，提交 `appKey`、`platform`、`currentVersionCode`；
2. **下载更新包**：从返回的 `downloadUrls` 下载到本地，支持进度回调与多地址容错；
3. **执行部署脚本**：可选，将全部下载文件绝对路径依次作为参数传入；脚本以后台方式启动，不等待其结束。

类库**不包含 main 方法**，以 API 形式供 GUI 程序、命令行工具、Web 服务等上层应用调用；
流程中的状态、进度与错误通过 `UpdateListener` 回调通知调用方。

依赖面：JDK 11+ 标准库 + Jackson（`jackson-databind`，版本由父 POM 统一管控），HTTP 通信使用 JDK 自带的 `HttpURLConnection`，无其他第三方依赖。

### 包结构

```
com.tbs.updater/
├── config/          配置类（UpdaterConfig）
├── model/           数据模型（UpdateInfo）
├── exception/       模块异常（UpdateException）
├── api/             契约接口与门面（UpdateListener、AutoUpdater）
└── impl/            默认实现（UpdateChecker、Downloader、ScriptExecutor）
```

## 快速开始

### Maven 引入

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>updater</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 编译打包

```bash
# 仓库根目录执行
mvn -pl updater clean package

# 产物：updater/target/updater-1.0-SNAPSHOT.jar
```

### 一键自动更新（Builder 配置）

```java
import com.tbs.updater.config.UpdaterConfig;
import com.tbs.updater.api.AutoUpdater;
import com.tbs.updater.api.UpdateListener;
import com.tbs.updater.model.UpdateInfo;

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
    public void onCheckStart() {
        System.out.println("检查更新中...");
    }

    @Override
    public void onCheckComplete(UpdateInfo info) {
        if (info.isNeedUpdate()) {
            System.out.println("发现新版本: " + info.getLatestVersionName());
        } else {
            System.out.println("当前已是最新版本");
        }
    }

    @Override
    public void onDownloadProgress(int progress) {
        System.out.print("\r下载进度: " + progress + "%");
    }

    @Override
    public void onSuccess() {
        System.out.println("\n更新流程完成");
    }

    @Override
    public void onError(String errMsg) {
        System.err.println("更新失败: " + errMsg);
    }
});

updater.autoUpdate();
```

### 分步控制（适合 GUI / 需要自定义编排的场景）

```java
import com.tbs.updater.api.AutoUpdater;
import com.tbs.updater.exception.UpdateException;
import com.tbs.updater.model.UpdateInfo;

// 在后台线程中执行，避免阻塞UI
new Thread(() -> {
    try {
        UpdateInfo info = updater.checkForUpdate();
        if (info.isNeedUpdate()) {
            File file = updater.downloadUpdate(info);
            updater.executeDeployScript(file);
        }
    } catch (UpdateException e) {
        // e.getErrorType() 区分错误类别，e.getMessage() 获取错误描述
    }
}).start();
```

## 配置说明

### Builder 配置项

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `serverHost` | String | 无（必填） | 服务端主机，可携带 `http://` / `https://` 协议头，缺省补 `http://` |
| `serverPort` | int | 0 | 服务端端口，小于等于 0 时 URL 不带端口 |
| `checkPath` | String | `/public/app-update/check` | 检查更新接口路径 |
| `appKey` | String | 无（必填） | 应用唯一标识 |
| `platform` | String | 无（必填） | 平台标识（如 `android`、`ios`） |
| `currentVersionCode` | int | 0 | 当前应用版本号 |
| `downloadDir` | String | 系统临时目录下 `updates` | 更新包下载目录 |
| `deployScript` | String | 空（跳过部署） | 部署脚本路径，后台启动不等待结束 |
| `timeoutSeconds` | int | 30 | 检查更新接口的连接/读取超时（秒） |
| `tryAllUrls` | boolean | true | 下载失败时是否依次尝试其余下载地址 |

### properties 文件配置

`fromFile(path)` 按 UTF-8 读取（支持中文值），`fromProperties(props)` 直接接收 `Properties` 对象。键名约定：

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
import com.tbs.updater.config.UpdaterConfig;
import com.tbs.updater.api.AutoUpdater;

UpdaterConfig config = UpdaterConfig.fromFile("/etc/updater/updater.properties");
AutoUpdater updater = new AutoUpdater(config, listener);
```

## 核心组件

| 组件 | 包 | 职责 |
|------|----|------|
| `UpdaterConfig` | `config` | 不可变配置类，Builder 构建 / properties 加载 / `buildCheckUrl()` 拼接接口地址 / `validate()` 校验必填项 |
| `UpdateInfo` | `model` | 更新信息模型，`parseFromJson(JsonNode)` 解析响应 data 节点 |
| `UpdateException` | `exception` | 统一异常，`ErrorType` 枚举：CONFIG_ERROR / NETWORK_ERROR / SERVER_ERROR / PARSE_ERROR / DOWNLOAD_ERROR / SCRIPT_ERROR |
| `UpdateListener` | `api` | 回调接口，全部方法 default 空实现，按需覆盖 |
| `AutoUpdater` | `api` | 门面类，提供 `autoUpdate()` 一键流程与 `checkForUpdate()` 等分步方法 |
| `UpdateChecker` | `impl` | POST 检查更新接口，校验 HTTP 状态与业务 code（200 为成功） |
| `Downloader` | `impl` | 流式下载 + 重定向跟随 + 进度回调 + 失败清理不完整文件 |
| `ScriptExecutor` | `impl` | ProcessBuilder 执行脚本，工作目录为脚本所在目录，stderr 合并输出实时透传 |

### 接口契约

- 检查更新接口：`POST http://{serverHost}:{serverPort}/public/app-update/check`，请求体 `{"appKey": "...", "platform": "...", "currentVersionCode": 0}`；
- 响应 `code` 固定 200 表示成功，其余值携带 `message` 抛出 `SERVER_ERROR`；
- 请求入参 `currentVersionCode` 为整数，响应 `latestVersionCode` 为字符串，`UpdateInfo` 统一按字符串持有。

## 最佳实践

1. **后台线程执行**：`autoUpdate()` 与分步方法均为阻塞调用，GUI 场景务必放到后台线程，回调中更新 UI 需自行切换回 UI 线程。
2. **部署脚本后台执行**：`executeDeployScript()` 以后台方式启动脚本、不等待其结束，脚本可直接用 `start`、`nohup` 等拉起常驻服务；部署是否成功由调用方（用户）确认。
3. **下载目录预检**：确保运行用户对 `downloadDir` 有写权限；下载失败时类库会自动清理不完整文件。
4. **可测试性**：`AutoUpdater` 提供五参构造函数，可注入自定义的 `UpdateChecker` / `Downloader` / `ScriptExecutor` 子类或 mock 进行测试。
5. **线程安全**：类本身不持有共享可变状态，`autoUpdate()` 顺序执行适合单线程调用；并发调用多个流程时由调用方自行同步。

## 故障排除

| 现象 | 错误类型 | 排查方向 |
|------|----------|----------|
| 缺少必填配置项 | CONFIG_ERROR | 检查 serverHost / appKey / platform / downloadDir 是否配置 |
| 检查更新网络请求失败 | NETWORK_ERROR | 网络连通性、serverHost/serverPort、超时时间 |
| 检查更新业务错误 | SERVER_ERROR | 服务端返回的 code/message，appKey 是否已登记 |
| 响应 JSON 解析失败 | PARSE_ERROR | 确认接口返回标准 `{code, message, data}` 结构 |
| 下载失败 | DOWNLOAD_ERROR | 下载地址签名是否过期、重定向 Location 是否完整、目录写权限 |
| 脚本执行失败 | SCRIPT_ERROR | 脚本是否存在、是否 `chmod +x`、退出码是否为 0 |

## GUI 演示

类库自带一个基于 Swing 的图形化更新器演示，展示进度条、状态标签、更新日志等完整交互：

```bash
# 编译后运行
mvn -pl updater clean compile
java -cp updater/target/classes:$(mvn -pl updater dependency:build-classpath -q -DincludeScope=runtime -Dmdep.outputFile=/dev/stdout) \
    com.tbs.updater.demo.UpdaterGuiDemo
```

源码位于 `com.tbs.updater.demo.UpdaterGuiDemo`，包含 `main` 方法，可直接运行。
