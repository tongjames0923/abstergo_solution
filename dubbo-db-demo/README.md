# dubbo-db-demo

基于 [dubbo-go-starter](../dubbo-go-starter/README.md) 的 **Go 轻量 Dubbo 提供者示例**：以一份 `service.json` 声明契约、一个 `main.go` 写业务实现，编译即为单二进制，注册到同一 Nacos 后由 Dubbo Admin 查询/测试调用、由 app 泛化链路消费（零 Java 接口 jar）。

> 独立 Go module，**不纳入根 `pom.xml`**；经 `go.mod` 的 `replace dubbo-go-starter => ../dubbo-go-starter` 引用库。
> 完整实现、契约字段、类型口径与部署验收见 [Go 轻量提供者指南](../docs/go-dubbo-provider-guide.md)，库的字段级说明见 [dubbo-go-starter/README.md](../dubbo-go-starter/README.md)。

## 目录

| 文件 | 说明 |
|------|------|
| `main.go` | 业务实现：`User` / `UserQuery` 两个 POJO、`UserProviderImpl` 的 5 个方法，以及 `starter.Run` 调用 |
| `service.json` | 契约配置：应用名/端口/Nacos group、接口与 5 个方法的参数与返回类型声明 |
| `api/` | 早期 Triple/IDL 试验遗留（`user.proto`、`user.pb.go`、`user.triple.go`），当前链路不依赖，保留不动 |

## 构建与运行

```powershell
go build -o dubbo-db-demo.exe .
.\dubbo-db-demo.exe
```

- 运行目录需含 `service.json`（IDE 运行配置的工作目录设为模块目录）；
- 环境变量：`NACOS_ADDRESS`（默认 `127.0.0.1:8848`）、`DUBBO_PORT`（默认取 `service.json` 的 `20001`）；
- 依赖 Nacos 可用；`etc/docker/docker-compose.yml` 提供 Nacos 与 Dubbo Admin；
- `Ctrl+C` 退出，Nacos 实例随心跳超时摘除。

## 方法清单

接口 `org.apache.dubbo.samples.api.UserProvider`（`version` / `group` 为空，端口 20001）：

| 方法 | `parameterTypes` | 返回 | 场景 |
|------|------------------|------|------|
| `Echo` | `java.lang.String` | `java.lang.String` | 标量 |
| `GetUser` | `java.lang.String` | `org.apache.dubbo.samples.api.User` | POJO 返回 |
| `FindByNames` | `java.util.List` | `java.util.List` | `List<String>` → `List<POJO>` |
| `FilterUsers` | `java.util.List`, `java.lang.String` | `java.util.List` | `List<复杂对象>`（对象含嵌套 `tags` 列表）+ 过滤 |
| `UpdateUser` | `org.apache.dubbo.samples.api.User` | 同 POJO | Map → struct → Map 往返 |

Dubbo Admin 测试调用 / app 泛化入口的请求体：

```json
{"name": "zhangsan"}
{"names": ["zhangsan", "lisi"]}
{"queries": [{"keyword":"alice","minAge":20,"tags":["vip","active"]},
             {"keyword":"bob","minAge":30,"tags":["active"]}], "tag": "vip"}
{"user": {"id": "1001", "name": "zhangsan", "age": 28}}
```

服务端日志会打印还原后的参数（`[FindByNames]` / `[FilterUsers]` / `[UpdateUser]`）；返回值为 `List<Map>` / `Map`，POJO 默认带 `class` 键，字段名由 `m` tag 决定。

类型串只用于 Java/Admin 侧登记（擦除 FQCN、不带泛型），**Go 侧参数还原依赖真实方法签名**：`[]string`、`[]*UserQuery`、`*User`。配置类型串与 Go 签名是两份声明，改动时须同步。

## 验收

1. Nacos 控制台服务列表出现 `go-user-provider`；
2. Dubbo Admin（9600）服务查询可见上表 5 个方法签名；
3. Admin 测试调用 `GetUser(zhangsan)` 返回 User 数据，列表/复杂方法按上述请求体返回预期结果；
4. 日志仅导出 `dubbo://:20001`（不出现 `tri://`）、无 `no metadata report factory` 警告与启动 ERROR。

**实测记录**（2026-11，本机 Nacos）：5 个方法通过启动期校验，`User` / `UserQuery` 自动注册，元数据发布与应用元数据刷新成功；dubbo-go 泛化客户端直连 20001 实测 `FindByNames` 返回 2 条、`FilterUsers` 按 `tag=vip` 过滤后仅返回 `alice`、`UpdateUser` 返回 `age=29` / `name=zhangsan(updated)`。Java 网关侧端到端泛化调用以开发者实机验证为准。
